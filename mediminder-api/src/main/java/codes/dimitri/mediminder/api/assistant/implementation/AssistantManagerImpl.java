package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.assistant.AssistantManager;
import codes.dimitri.mediminder.api.assistant.AssistantRequestDTO;
import codes.dimitri.mediminder.api.assistant.AssistantResponseDTO;
import codes.dimitri.mediminder.api.assistant.InvalidAssistantException;
import codes.dimitri.mediminder.api.cabinet.CabinetEntryDTO;
import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.schedule.EventDTO;
import codes.dimitri.mediminder.api.schedule.EventManager;
import codes.dimitri.mediminder.api.schedule.ScheduleDTO;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
class AssistantManagerImpl implements AssistantManager {
    private static final DateTimeFormatter HUMAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private final ChatClient chatClient;
    private final MedicationManager medicationManager;
    private final ScheduleManager scheduleManager;
    private final ObjectWriter objectWriter;
    private final UserManager userManager;
    private final EventManager eventManager;
    private final CabinetEntryManager cabinetEntryManager;
    private final AssistantProperties properties;
    private final AssistantMapper assistantMapper;

    public AssistantManagerImpl(ChatClient chatClient, MedicationManager medicationManager, ScheduleManager scheduleManager, ObjectMapper objectMapper, UserManager userManager, EventManager eventManager, CabinetEntryManager cabinetEntryManager, AssistantProperties properties, AssistantMapper assistantMapper) {
        this.chatClient = chatClient;
        this.medicationManager = medicationManager;
        this.scheduleManager = scheduleManager;
        this.objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        this.userManager = userManager;
        this.eventManager = eventManager;
        this.cabinetEntryManager = cabinetEntryManager;
        this.properties = properties;
        this.assistantMapper = assistantMapper;
    }

    @Override
    @Retryable(retryFor = MismatchedInputException.class)
    public AssistantResponseDTO answer(@Valid @NotNull AssistantRequestDTO request) {
        UserDTO user = findCurrentUser();
        LocalDateTime today = userManager.calculateTodayForUser(user.id());
        Map<String, Object> variables = Map.of(
            "medicationsJson", getNestedMedication(today),
            "question", request.question(),
            "name", user.name() == null ? "unknown" : user.name(),
            "today", HUMAN_DATE_FORMATTER.format(today)
        );
        return this.chatClient
            .prompt()
            .advisors(advisors -> advisors.param(CHAT_MEMORY_CONVERSATION_ID_KEY, user.id()))
            .user(prompt -> prompt.params(variables))
            .call()
            .entity(AssistantResponseDTO.class);
    }

    private UserDTO findCurrentUser() {
        try {
            return userManager.findCurrentUser();
        } catch (CurrentUserNotFoundException ex) {
            throw new InvalidAssistantException(ex);
        }
    }

    @SneakyThrows
    private String getNestedMedication(LocalDateTime today) {
        var pageable = PageRequest.ofSize(properties.maxSize());
        List<MedicationDTO> medications = getMedication(pageable);
        List<ScheduleDTO> schedules = getSchedules(pageable);
        List<CabinetEntryDTO> cabinetEntries = getCabinetEntries(pageable);
        List<EventDTO> eventsToday = eventManager.findAll(today.toLocalDate());

        List<AssistantMedicationInfo> medicationInfos = medications
            .stream()
            .map(medication -> mapToMedicationInfo(medication, schedules, cabinetEntries, eventsToday))
            .toList();
        return objectWriter.writeValueAsString(medicationInfos);
    }

    private AssistantMedicationInfo mapToMedicationInfo(MedicationDTO medication,
                                                        List<ScheduleDTO> allSchedules,
                                                        List<CabinetEntryDTO> allCabinetEntries,
                                                        List<EventDTO> allEventsToday) {
        List<AssistantScheduleInfo> schedules = getSchedulesForMedication(allSchedules, medication);
        List<AssistantCabinetEntryInfo> cabinetEntries = getCabinetEntriesForMedication(allCabinetEntries, medication);
        List<AssistantEventInfo> eventsToday = getEventsForMedication(allEventsToday, medication);
        return assistantMapper.toAssistantMedicationInfo(medication, schedules, cabinetEntries, eventsToday);
    }

    private List<MedicationDTO> getMedication(PageRequest pageable) {
        return medicationManager
            .findAllForCurrentUser(null, pageable)
            .getContent();
    }

    private List<ScheduleDTO> getSchedules(PageRequest pageable) {
        return scheduleManager
            .findAllForCurrentUser(null, pageable)
            .getContent();
    }

    private List<CabinetEntryDTO> getCabinetEntries(PageRequest pageable) {
        return cabinetEntryManager
            .findAllForCurrentUser(null, pageable)
            .getContent();
    }

    private List<AssistantScheduleInfo> getSchedulesForMedication(List<ScheduleDTO> schedules, MedicationDTO medication) {
        return schedules.stream()
            .filter(schedule -> schedule.medication().id().equals(medication.id()))
            .map(assistantMapper::toAssistantScheduleInfo)
            .toList();
    }

    private List<AssistantCabinetEntryInfo> getCabinetEntriesForMedication(List<CabinetEntryDTO> cabinetEntries, MedicationDTO medication) {
        return cabinetEntries.stream()
            .filter(cabinetEntry -> cabinetEntry.medication().id().equals(medication.id()))
            .map(assistantMapper::toAssistantCabinetEntryInfo)
            .toList();
    }

    private List<AssistantEventInfo> getEventsForMedication(List<EventDTO> events, MedicationDTO medication) {
        return events.stream()
            .filter(event -> event.medication().id().equals(medication.id()))
            .map(assistantMapper::toAssistantEventInfo)
            .toList();
    }
}
