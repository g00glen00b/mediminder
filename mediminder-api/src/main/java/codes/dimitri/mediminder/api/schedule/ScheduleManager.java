package codes.dimitri.mediminder.api.schedule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ScheduleManager {
    Page<ScheduleDTO> findAllForCurrentUser(UUID medicationId, boolean onlyActive, @NotNull Pageable pageable);

    Page<ScheduleDTO> findAllWithinPeriod(@Valid @NotNull SchedulePeriodDTO period, @NotNull Pageable pageable);

    @Transactional
    ScheduleDTO createForCurrentUser(@Valid @NotNull CreateScheduleRequestDTO request);

    @Transactional
    ScheduleDTO updateForCurrentUser(@NotNull UUID id, @Valid @NotNull UpdateScheduleRequestDTO request);

    @Transactional
    void deleteForCurrentUser(@NotNull UUID id);

    @Transactional
    void deleteAllByMedicationId(@NotNull UUID medicationId);

    BigDecimal calculateRequiredDoses(@NotNull UUID medicationId, @NotNull @Valid SchedulePeriodDTO period);

    ScheduleDTO findByIdForCurrentUser(@NotNull UUID id);

    Page<UserScheduledMedicationDTO> findAllUserScheduledMedicationOnDate(@NotNull LocalDate targetDate, @NotNull Pageable pageable);
}
