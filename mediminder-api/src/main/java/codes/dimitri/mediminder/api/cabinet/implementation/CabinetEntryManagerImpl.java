package codes.dimitri.mediminder.api.cabinet.implementation;

import codes.dimitri.mediminder.api.cabinet.*;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.medication.MedicationNotFoundException;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BinaryOperator;

@Validated
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
class CabinetEntryManagerImpl implements CabinetEntryManager {
    private final UserManager userManager;
    private final MedicationManager medicationManager;
    private final CabinetEntryEntityRepository repository;
    private final CabinetEntryMapper mapper;

    @Override
    @Transactional
    public CabinetEntryDTO createForCurrentUser(@Valid @NotNull CreateCabinetEntryRequestDTO request) {
        UserDTO currentUser = findCurrentUser();
        MedicationDTO medication = findMedication(request.medicationId());
        validateRemainingDoses(request.remainingDoses(), medication);
        CabinetEntryEntity entity = new CabinetEntryEntity(
            currentUser.id(),
            medication.id(),
            request.remainingDoses(),
            request.expiryDate()
        );
        return mapper.toDTO(repository.save(entity), medication);
    }

    @Override
    public Page<CabinetEntryDTO> findAllForCurrentUser(UUID medicationId, @NotNull Pageable pageable) {
        return findAllEntities(medicationId, pageable)
            .map(this::mapEntityToDTO);
    }

    private Page<CabinetEntryEntity> findAllEntities(UUID medicationId, Pageable pageable) {
        UserDTO currentUser = findCurrentUser();
        if (medicationId == null) {
            return repository.findAllByUserId(currentUser.id(), pageable);
        } else {
            return repository.findAllByMedicationIdAndUserId(medicationId, currentUser.id(), pageable);
        }
    }

    private CabinetEntryDTO mapEntityToDTO(CabinetEntryEntity entity) {
        MedicationDTO medication = findMedicationOrEmtpy(entity.getMedicationId(), entity.getUserId());
        return mapper.toDTO(entity, medication);
    }

    private MedicationDTO findMedicationOrEmtpy(UUID medicationId, String userId) {
        try {
            return medicationManager.findByIdAndUserId(medicationId, userId);
        } catch (MedicationNotFoundException ex) {
            return null;
        }
    }

    @Override
    public CabinetEntryDTO findByIdForCurrentUser(@NotNull UUID id) {
        UserDTO user = findCurrentUser();
        CabinetEntryEntity entity = findEntity(id, user);
        return mapEntityToDTO(entity);
    }

    @Override
    @Transactional
    public CabinetEntryDTO updateForCurrentUser(@NotNull UUID id, @Valid @NotNull UpdateCabinetEntryRequestDTO request) {
        UserDTO currentUser = findCurrentUser();
        CabinetEntryEntity entity = findEntity(id, currentUser);
        MedicationDTO medication = findMedication(entity.getMedicationId());
        validateRemainingDoses(request.remainingDoses(), medication);
        entity.setExpiryDate(request.expiryDate());
        entity.setRemainingDoses(request.remainingDoses());
        return mapper.toDTO(entity, medication);
    }

    @Override
    @Transactional
    public void deleteForCurrentUser(@NotNull UUID id) {
        UserDTO currentUser = findCurrentUser();
        CabinetEntryEntity entity = findEntity(id, currentUser);
        repository.delete(entity);
    }

    private CabinetEntryEntity findEntity(UUID id, UserDTO currentUser) {
        return repository
            .findByIdAndUserId(id, currentUser.id())
            .orElseThrow(() -> new CabinetEntryNotFoundException(id));
    }

    private MedicationDTO findMedication(UUID medicationId) {
        try {
            return medicationManager.findByIdForCurrentUser(medicationId);
        } catch (MedicationNotFoundException ex) {
            throw new InvalidCabinetEntryException("Medication is not found", ex);
        }
    }

    private UserDTO findCurrentUser() {
        try {
            return userManager.findCurrentUser();
        } catch (CurrentUserNotFoundException ex) {
            throw new InvalidCabinetEntryException("User is not authenticated", ex);
        }
    }

    private static void validateRemainingDoses(BigDecimal remainingDoses, MedicationDTO medication) {
        if (remainingDoses.compareTo(medication.dosesPerPackage()) > 0) {
            throw new InvalidCabinetEntryException("Remaining doses cannot be more than the initial doses per package");
        }
    }

    @Override
    public BigDecimal calculateTotalRemainingDosesByMedicationId(@NotNull UUID medicationId) {
        BigDecimal result = repository.sumRemainingDosesByMedicationId(medicationId);
        if (result == null) return BigDecimal.ZERO;
        return result;
    }

    @Override
    @Transactional
    public void deleteAllByMedicationId(@NotNull UUID medicationId) {
        repository.deleteAllByMedicationId(medicationId);
    }

    @Override
    @Transactional(noRollbackFor = InvalidCabinetEntryException.class)
    public void subtractDosesByMedicationId(@NotNull UUID medicationId, @NotNull @PositiveOrZero BigDecimal doses) {
        Page<CabinetEntryEntity> entries = findEntriesByMedicationIdSortedByExpiryDate(medicationId);
        reduceDosesFromEntries(doses, entries);
        deleteEmptyEntries(entries);
    }

    private Page<CabinetEntryEntity> findEntriesByMedicationIdSortedByExpiryDate(UUID medicationId) {
        PageRequest pageRequest = PageRequest.of(0, 20, Sort.Direction.ASC, "expiryDate");
        return repository.findAllWithRemainingDosesByMedicationId(medicationId, pageRequest);
    }

    private static BigDecimal reduceDosesFromEntries(BigDecimal doses, Streamable<CabinetEntryEntity> entries) {
        return entries.stream().reduce(
            doses,
            (remainder, entry) -> entry.subtractDoses(remainder),
            unsupportedOperator());
    }

    private void deleteEmptyEntries(Streamable<CabinetEntryEntity> entries) {
        List<CabinetEntryEntity> emptyEntries = entries.stream().filter(CabinetEntryEntity::isEmpty).toList();
        if (!emptyEntries.isEmpty()) repository.deleteAll(emptyEntries);
    }

    @Override
    @Transactional
    public void addDosesByMedicationId(@NotNull UUID medicationId, @NotNull @PositiveOrZero BigDecimal doses) {
        findOldestEntryByMedicationId(medicationId).ifPresent(entity -> entity.addDoses(doses));
    }

    private Optional<CabinetEntryEntity> findOldestEntryByMedicationId(UUID medicationId) {
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.Direction.ASC, "expiryDate");
        Page<CabinetEntryEntity> results = repository.findAllByMedicationId(medicationId, pageRequest);
        return results.stream().findAny();
    }

    private static <T> BinaryOperator<T> unsupportedOperator() {
        return (arg1, arg2) -> {throw new UnsupportedOperationException();};
    }

    @Override
    public Page<CabinetEntryDTO> findAllNonEmptyWithExpiryDateBefore(@NotNull LocalDate targetDate, @NotNull Pageable pageable) {
        return repository
            .findAllWithRemainingDosesWithExpiryDateBefore(targetDate, pageable)
            .map(this::mapEntityToDTO);
    }
}
