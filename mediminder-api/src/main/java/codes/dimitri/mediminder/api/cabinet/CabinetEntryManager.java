package codes.dimitri.mediminder.api.cabinet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CabinetEntryManager {
    @Transactional
    CabinetEntryDTO createForCurrentUser(@Valid @NotNull CreateCabinetEntryRequestDTO request);

    Page<CabinetEntryDTO> findAllForCurrentUser(@NotNull Pageable pageable);

    CabinetEntryDTO findByIdForCurrentUser(@NotNull UUID id);

    @Transactional
    CabinetEntryDTO updateForCurrentUser(@NotNull UUID id, @Valid @NotNull UpdateCabinetEntryRequestDTO request);

    @Transactional
    void deleteForCurrentUser(@NotNull UUID id);

    BigDecimal calculateTotalRemainingDosesByMedicationId(@NotNull UUID medicationId);

    @Transactional
    void deleteAllByMedicationId(@NotNull UUID medicationId);

    @Transactional
    void subtractDosesByMedicationId(@NotNull UUID medicationId, @NotNull @PositiveOrZero BigDecimal doses);

    @Transactional
    void addDosesByMedicationId(@NotNull UUID medicationId, @NotNull @PositiveOrZero BigDecimal doses);

    Page<CabinetEntryDTO> findAllNonEmptyWithExpiryDateBefore(@NotNull LocalDate targetDate, Pageable pageable);
}
