package codes.dimitri.mediminder.api.planner.implementation;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.planner.InvalidPlannerException;
import codes.dimitri.mediminder.api.planner.MedicationPlannerDTO;
import codes.dimitri.mediminder.api.planner.PlannerManager;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.schedule.SchedulePeriodDTO;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
class PlannerManagerImpl implements PlannerManager {
    private final CabinetEntryManager cabinetEntryManager;
    private final ScheduleManager scheduleManager;
    private final MedicationManager medicationManager;
    private final UserManager userManager;

    @Override
    public Page<MedicationPlannerDTO> findAll(@NotNull LocalDate targetDate, @NotNull Pageable pageable) {
        UserDTO user = findCurrentUser();
        LocalDate today = userManager.calculateTodayForUser(user.id()).toLocalDate();
        SchedulePeriodDTO period = new SchedulePeriodDTO(today, targetDate);
        return medicationManager
            .findAllForCurrentUser(null, pageable)
            .map(medication -> {
                BigDecimal availableDoses = cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(medication.id());
                BigDecimal requiredDoses = scheduleManager.calculateRequiredDoses(medication.id(), period);
                return new MedicationPlannerDTO(medication, availableDoses, requiredDoses);
            });
    }

    private UserDTO findCurrentUser() {
        try {
            return userManager.findCurrentUser();
        } catch (CurrentUserNotFoundException ex) {
            throw new InvalidPlannerException("User is not authenticated", ex);
        }
    }
}
