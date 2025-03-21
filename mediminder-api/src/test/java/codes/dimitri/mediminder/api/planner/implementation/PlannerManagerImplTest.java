package codes.dimitri.mediminder.api.planner.implementation;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.planner.InvalidPlannerException;
import codes.dimitri.mediminder.api.planner.MedicationPlannerDTO;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.schedule.SchedulePeriodDTO;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2"
})
@Transactional
class PlannerManagerImplTest {
    @Autowired
    private PlannerManagerImpl plannerManager;
    @MockitoBean
    private UserManager userManager;
    @MockitoBean
    private MedicationManager medicationManager;
    @MockitoBean
    private ScheduleManager scheduleManager;
    @MockitoBean
    private CabinetEntryManager cabinetEntryManager;

    @Nested
    class findAll {
        @Test
        void returnsResults() {
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var today = LocalDateTime.of(2024, 6, 30, 10, 0);
            var targetDate = LocalDate.of(2024, 7, 10);
            var expectedPeriod = new SchedulePeriodDTO(today.toLocalDate(), targetDate);
            var pageRequest = PageRequest.of(0, 10);
            var medication1 = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var medication2 = new MedicationDTO(
                UUID.randomUUID(),
                "Hydrocortisone 14mg",
                new MedicationTypeDTO("CAPSULE", "Capsule"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("CAPSULE", "capsule(s)"),
                new BigDecimal("60"),
                Color.YELLOW
            );
            var medicationPage = new PageImpl<>(List.of(medication1, medication2));
            when(userManager.findCurrentUser()).thenReturn(user);
            when(userManager.calculateTodayForUser(user.id())).thenReturn(today);
            when(medicationManager.findAllForCurrentUser(null, pageRequest)).thenReturn(medicationPage);
            when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(medication1.id())).thenReturn(new BigDecimal("30"));
            when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(medication2.id())).thenReturn(new BigDecimal("5"));
            when(scheduleManager.calculateRequiredDoses(medication1.id(), expectedPeriod)).thenReturn(new BigDecimal("20"));
            when(scheduleManager.calculateRequiredDoses(medication2.id(), expectedPeriod)).thenReturn(new BigDecimal("10"));
            Page<MedicationPlannerDTO> results = plannerManager.findAll(targetDate, pageRequest);
            assertThat(results).containsExactly(
                new MedicationPlannerDTO(medication1, new BigDecimal("30"), new BigDecimal("20")),
                new MedicationPlannerDTO(medication2, new BigDecimal("5"), new BigDecimal("10")));
        }

        @Test
        void failsIfUserNotAuthenticated() {
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidPlannerException.class)
                .isThrownBy(() -> plannerManager.findAll(LocalDate.now(), PageRequest.of(0, 10)))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfTargetDateNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> plannerManager.findAll(null, PageRequest.of(0, 10)));
        }

        @Test
        void failsIfPageRequestNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> plannerManager.findAll(LocalDate.now(), null));
        }
    }
}