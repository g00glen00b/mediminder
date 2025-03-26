package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.medication.MedicationNotFoundException;
import codes.dimitri.mediminder.api.schedule.*;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
class ScheduleManagerImpl implements ScheduleManager {
    private final ScheduleEntityRepository repository;
    private final MedicationManager medicationManager;
    private final UserManager userManager;
    private final ScheduleEntityMapper mapper;

    @Override
    public Page<ScheduleDTO> findAllForCurrentUser(UUID medicationId, @NotNull Pageable pageable) {
        return findAllScheduleEntities(medicationId, pageable)
            .map(this::mapEntityToDTO);
    }

    private Page<ScheduleEntity> findAllScheduleEntities(UUID medicationId, Pageable pageable) {
        UserDTO user = findCurrentUser();
        if (medicationId == null) {
            return repository.findAllByUserId(user.id(), pageable);
        } else {
            return repository.findAllByMedicationIdAndUserId(medicationId, user.id(), pageable);
        }
    }

    @Override
    @Transactional
    public ScheduleDTO createForCurrentUser(@Valid @NotNull CreateScheduleRequestDTO request) {
        UserDTO user = findCurrentUser();
        MedicationDTO medication = findMedication(request.medicationId(), user.id());
        SchedulePeriodEntity period = SchedulePeriodEntity.of(request.period().startingAt(), request.period().endingAtInclusive());
        ScheduleEntity entity = new ScheduleEntity(
            user.id(),
            medication.id(),
            period,
            request.interval(),
            request.time(),
            request.description(),
            request.dose()
        );
        return mapper.toDTO(repository.save(entity), medication);
    }

    @Override
    @Transactional
    public ScheduleDTO updateForCurrentUser(@NotNull UUID id, @Valid @NotNull UpdateScheduleRequestDTO request) {
        UserDTO user = findCurrentUser();
        ScheduleEntity entity = findEntity(id, user);
        MedicationDTO medication = findMedication(entity.getMedicationId(), user.id());
        SchedulePeriodEntity period = SchedulePeriodEntity.of(request.period().startingAt(), request.period().endingAtInclusive());
        entity.setDescription(request.description());
        entity.setInterval(request.interval());
        entity.setPeriod(period);
        entity.setTime(request.time());
        entity.setDose(request.dose());
        return mapper.toDTO(entity, medication);
    }

    @Override
    @Transactional
    public void deleteForCurrentUser(@NotNull UUID id) {
        UserDTO user = findCurrentUser();
        ScheduleEntity entity = findEntity(id, user);
        repository.delete(entity);
    }

    @Override
    public BigDecimal calculateRequiredDoses(@NotNull UUID medicationId, @NotNull @Valid SchedulePeriodDTO period) {
        List<ScheduleEntity> schedules = repository.findAllByMedicationIdAndDateInPeriodGroup(
            period.startingAt(),
            period.endingAtInclusive(),
            medicationId);
        return schedules
            .stream()
            .map(entity -> entity.calculateTakenDosesInPeriod(period.startingAt(), period.endingAtInclusive()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private UserDTO findCurrentUser() {
        try {
            return userManager.findCurrentUser();
        } catch (CurrentUserNotFoundException ex) {
            throw new InvalidScheduleException("User is not authenticated", ex);
        }
    }

    private MedicationDTO findMedication(UUID medicationId, UUID userId) {
        try {
            return medicationManager.findByIdAndUserId(medicationId, userId);
        } catch (MedicationNotFoundException ex) {
            throw new InvalidScheduleException("Medication is not found", ex);
        }
    }

    private ScheduleEntity findEntity(UUID id, UserDTO currentUser) {
        return repository
            .findByIdAndUserId(id, currentUser.id())
            .orElseThrow(() -> new ScheduleNotFoundException(id));
    }

    @Override
    @Transactional
    public void deleteAllByMedicationId(@NotNull UUID medicationId) {
        repository.deleteAllByMedicationId(medicationId);
    }

    @Override
    public ScheduleDTO findByIdForCurrentUser(@NotNull UUID id) {
        UserDTO user = findCurrentUser();
        ScheduleEntity entity = findEntity(id, user);
        return mapEntityToDTO(entity);
    }

    @Override
    public Page<UserScheduledMedicationDTO> findAllUserScheduledMedicationOnDate(@NotNull LocalDate targetDate, @NotNull Pageable pageable) {
        return repository.findAllWithUserScheduledMedicationOnDate(targetDate, pageable);
    }

    @Override
    public Page<ScheduleDTO> findAllWithinPeriod(@Valid @NotNull SchedulePeriodDTO period, @NotNull Pageable pageable) {
        return repository
            .findAllByOverlappingPeriod(period.startingAt(), period.endingAtInclusive(), pageable)
            .map(this::mapEntityToDTO);
    }

    private ScheduleDTO mapEntityToDTO(ScheduleEntity entity) {
        MedicationDTO medication = findMedicationOrEmpty(entity.getMedicationId(), entity.getUserId());
        return mapper.toDTO(entity, medication);
    }

    private MedicationDTO findMedicationOrEmpty(UUID medicationId, UUID userId) {
        try {
            return medicationManager.findByIdAndUserId(medicationId, userId);
        } catch (MedicationNotFoundException ex) {
            return null;
        }
    }
}
