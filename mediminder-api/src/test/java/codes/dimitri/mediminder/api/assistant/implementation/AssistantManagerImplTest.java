package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.assistant.AssistantManager;
import codes.dimitri.mediminder.api.assistant.AssistantRequestDTO;
import codes.dimitri.mediminder.api.assistant.AssistantResponseDTO;
import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.schedule.ScheduleDTO;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.schedule.SchedulePeriodDTO;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.ai.openai.api-key=dummy"
})
@Import({
    TestOpenAIConfiguration.class
})
@Transactional
class AssistantManagerImplTest {
    @Autowired
    private AssistantManager assistantManager;
    @MockitoBean
    private MedicationManager medicationManager;
    @MockitoBean
    private UserManager userManager;
    @MockitoBean
    private ScheduleManager scheduleManager;

    @Nested
    class answer {
        @Test
        void returnsAnswer() {
            var request = new AssistantRequestDTO("When do I have to take Dafalgan?");
            var today = LocalDateTime.of(2025, 3, 4, 10, 0);
            var pageRequest = PageRequest.of(0, 20);
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication1 = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("1"),
                Color.RED
            );
            var medication2 = new MedicationDTO(
                UUID.randomUUID(),
                "Hydrocortisone 8mg",
                new MedicationTypeDTO("CAPSULE", "Capsule"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("CAPSULE", "capsule(s)"),
                new BigDecimal("1"),
                Color.YELLOW
            );
            var schedule = new ScheduleDTO(
                UUID.randomUUID(),
                user.id(),
                medication2,
                Period.ofDays(1),
                new SchedulePeriodDTO(LocalDate.of(2025, 1, 1), null),
                "Before lunch",
                BigDecimal.ONE,
                LocalTime.of(8, 0)
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(userManager.calculateTodayForUser(user.id())).thenReturn(today);
            when(medicationManager.findAllForCurrentUser(null, pageRequest)).thenReturn(new PageImpl<>(List.of(medication1, medication2)));
            when(scheduleManager.findAllForCurrentUser(pageRequest)).thenReturn(new PageImpl<>(List.of(schedule)));
            AssistantResponseDTO answer = assistantManager.answer(request);
            assertThat(answer).isEqualTo(new AssistantResponseDTO("Hello, how can I assist you today?"));
        }
    }
}