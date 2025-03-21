package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.assistant.AssistantManager;
import codes.dimitri.mediminder.api.assistant.AssistantRequestDTO;
import codes.dimitri.mediminder.api.assistant.AssistantResponseDTO;
import codes.dimitri.mediminder.api.cabinet.CabinetEntryDTO;
import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.schedule.*;
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
    "spring.ai.openai.api-key=dummy",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2"
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
    @MockitoBean
    private CabinetEntryManager cabinetEntryManager;
    @MockitoBean
    private EventManager eventManager;

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
            var cabinetEntry = new CabinetEntryDTO(
                UUID.randomUUID(),
                user.id(),
                medication2,
                new BigDecimal("10"),
                LocalDate.of(2025, 6, 30)
            );
            var event = new EventDTO(
                UUID.randomUUID(),
                schedule.id(),
                medication2,
                today,
                today.plusMinutes(1),
                schedule.dose(),
                schedule.description()
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(userManager.calculateTodayForUser(user.id())).thenReturn(today);
            when(medicationManager.findAllForCurrentUser(null, pageRequest)).thenReturn(new PageImpl<>(List.of(medication1, medication2)));
            when(scheduleManager.findAllForCurrentUser(pageRequest)).thenReturn(new PageImpl<>(List.of(schedule)));
            when(cabinetEntryManager.findAllForCurrentUser(pageRequest)).thenReturn(new PageImpl<>(List.of(cabinetEntry)));
            when(eventManager.findAll(today.toLocalDate())).thenReturn(List.of(event));
            AssistantResponseDTO answer = assistantManager.answer(request);
            // Response is based upon assertions made in src/test/resources/wiremock/mappings/openai.json
            assertThat(answer).isEqualTo(new AssistantResponseDTO("Hello, how can I assist you today?"));
        }

        @Test
        void returnsAnswerForReasoningModel() {
            var request = new AssistantRequestDTO("When do I have to take Dafalgan if you had to reason?");
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
            var cabinetEntry = new CabinetEntryDTO(
                UUID.randomUUID(),
                user.id(),
                medication2,
                new BigDecimal("10"),
                LocalDate.of(2025, 6, 30)
            );
            var event = new EventDTO(
                UUID.randomUUID(),
                schedule.id(),
                medication2,
                today,
                today.plusMinutes(1),
                schedule.dose(),
                schedule.description()
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(userManager.calculateTodayForUser(user.id())).thenReturn(today);
            when(medicationManager.findAllForCurrentUser(null, pageRequest)).thenReturn(new PageImpl<>(List.of(medication1, medication2)));
            when(scheduleManager.findAllForCurrentUser(pageRequest)).thenReturn(new PageImpl<>(List.of(schedule)));
            when(cabinetEntryManager.findAllForCurrentUser(pageRequest)).thenReturn(new PageImpl<>(List.of(cabinetEntry)));
            when(eventManager.findAll(today.toLocalDate())).thenReturn(List.of(event));
            AssistantResponseDTO answer = assistantManager.answer(request);
            // Response is based upon assertions made in src/test/resources/wiremock/mappings/openai-reasoning.json
            assertThat(answer).isEqualTo(new AssistantResponseDTO("Hello, how can I assist you today if I reasoned?"));
        }
    }
}