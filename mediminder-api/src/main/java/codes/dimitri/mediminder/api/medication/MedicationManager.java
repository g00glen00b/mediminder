package codes.dimitri.mediminder.api.medication;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface MedicationManager {
    @Transactional
    MedicationDTO createForCurrentUser(@Valid @NotNull CreateMedicationRequestDTO request);

    Page<MedicationDTO> findAllForCurrentUser(String search, @NotNull Pageable pageable);

    MedicationDTO findByIdForCurrentUser(@NotNull UUID id);

    MedicationDTO findByIdAndUserId(@NotNull UUID id, @NotNull UUID userId);

    @Transactional
    void deleteByIdForCurrentUser(@NotNull UUID id);

    @Transactional
    MedicationDTO updateForCurrentUser(@NotNull UUID id, @Valid @NotNull UpdateMedicationRequestDTO request);

    void deleteAllByUserId(@NotNull UUID userId);
}
