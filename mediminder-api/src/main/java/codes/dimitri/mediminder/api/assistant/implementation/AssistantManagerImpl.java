package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.assistant.AssistantManager;
import codes.dimitri.mediminder.api.assistant.AssistantRequestDTO;
import codes.dimitri.mediminder.api.assistant.AssistantResponseDTO;
import codes.dimitri.mediminder.api.assistant.InvalidAssistantException;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.schedule.ScheduleDTO;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
class AssistantManagerImpl implements AssistantManager {
    private static final DateTimeFormatter HUMAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private final ChatClient chatClient;
    private final MedicationManager medicationManager;
    private final ScheduleManager scheduleManager;
    private final ObjectMapper objectMapper;
    private final UserManager userManager;
    private final AssistantProperties properties;
    private final AssistantMapper mapper;
    private final AssistantMapper assistantMapper;

    @Override
    @Retryable(retryFor = MismatchedInputException.class)
    public AssistantResponseDTO answer(@Valid @NotNull AssistantRequestDTO request) {
        UserDTO user = findCurrentUser();
        Map<String, Object> variables = Map.of(
            "medicationsJson", getNestedMedication(),
            "question", request.question(),
            "name", user.name(),
            "today", getToday(user)
        );
        return this.chatClient
            .prompt()
            .advisors(advisors -> advisors.param(CHAT_MEMORY_CONVERSATION_ID_KEY, user.id()))
            .user(prompt -> prompt.params(variables))
            .call()
            .entity(AssistantResponseDTO.class);
    }

    private UserDTO findCurrentUser() {
        return userManager.findCurrentUser()
            .orElseThrow(() -> new InvalidAssistantException("User is not authenticated"));
    }

    @SneakyThrows
    private String getNestedMedication() {
        var pageable = PageRequest.ofSize(properties.maxSize());
        List<MedicationDTO> medications = getMedication(pageable);
        List<ScheduleDTO> schedules = getSchedules(pageable);
        List<AssistantMedicationInfo> medicationInfos = medications
            .stream()
            .map(medication -> mapToMedicationInfo(medication, schedules))
            .toList();
        return objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(medicationInfos);
    }

    private AssistantMedicationInfo mapToMedicationInfo(MedicationDTO medication, List<ScheduleDTO> allSchedules) {
        List<AssistantScheduleInfo> schedules = getSchedulesForMedication(allSchedules, medication);
        return assistantMapper.toAssistantMedicationInfo(medication, schedules);
    }

    private List<MedicationDTO> getMedication(PageRequest pageable) {
        return medicationManager
            .findAllForCurrentUser(null, pageable)
            .getContent();
    }

    private List<ScheduleDTO> getSchedules(PageRequest pageable) {
        return scheduleManager
            .findAllForCurrentUser(pageable)
            .getContent();
    }

    private List<AssistantScheduleInfo> getSchedulesForMedication(List<ScheduleDTO> schedules, MedicationDTO medication) {
        return schedules.stream()
            .filter(schedule -> schedule.medication().id().equals(medication.id()))
            .map(assistantMapper::toAssistantScheduleInfo)
            .toList();
    }

    private String getToday(UserDTO user) {
        LocalDateTime today = userManager.calculateTodayForUser(user.id());
        return HUMAN_DATE_FORMATTER.format(today);

    }
}
