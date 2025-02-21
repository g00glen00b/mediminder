package codes.dimitri.mediminder.api.planner.implementation;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.planner.InvalidPlannerException;
import codes.dimitri.mediminder.api.planner.MedicationPlannerDTO;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.schedule.SchedulePeriodDTO;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlannerManagerImplTest {
    @InjectMocks
    private PlannerManagerImpl manager;
    @Mock
    private UserManager userManager;
    @Mock
    private CabinetEntryManager cabinetEntryManager;
    @Mock
    private ScheduleManager scheduleManager;
    @Mock
    private MedicationManager medicationManager;

    @Nested
    class findAll {
        @Test
        void returnsResult() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var targetDate = LocalDate.of(2024, 9, 30);
            var today = LocalDateTime.of(2024, 6, 30, 10, 0);
            var medication = Instancio.create(MedicationDTO.class);
            var remainingDoses = new BigDecimal("100");
            var requiredDoses = new BigDecimal("30");
            var pageRequest = PageRequest.of(0, 20);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(userManager.calculateTodayForUser(any())).thenReturn(today);
            when(medicationManager.findAllForCurrentUser(any(), any())).thenReturn(new PageImpl<>(List.of(medication)));
            when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(any())).thenReturn(remainingDoses);
            when(scheduleManager.calculateRequiredDoses(any(), any())).thenReturn(requiredDoses);
            // Then
            Page<MedicationPlannerDTO> result = manager.findAll(targetDate, pageRequest);
            assertThat(result.getContent()).containsOnly(new MedicationPlannerDTO(
                medication,
                remainingDoses,
                requiredDoses
            ));
            verify(userManager).findCurrentUser();
            verify(userManager).calculateTodayForUser(user.id());
            verify(medicationManager).findAllForCurrentUser(null, pageRequest);
            verify(cabinetEntryManager).calculateTotalRemainingDosesByMedicationId(medication.id());
            verify(scheduleManager).calculateRequiredDoses(medication.id(), new SchedulePeriodDTO(today.toLocalDate(), targetDate));
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var targetDate = LocalDate.of(2024, 9, 30);
            var pageRequest = PageRequest.of(0, 20);
            // Then
            assertThatExceptionOfType(InvalidPlannerException.class)
                .isThrownBy(() -> manager.findAll(targetDate, pageRequest))
                .withMessage("User is not authenticated");
        }
    }
}