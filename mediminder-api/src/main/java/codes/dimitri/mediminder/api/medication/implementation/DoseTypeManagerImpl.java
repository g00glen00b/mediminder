package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.DoseTypeDTO;
import codes.dimitri.mediminder.api.medication.DoseTypeManager;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
class DoseTypeManagerImpl implements DoseTypeManager {
    private final DoseTypeEntityRepository repository;
    private final DoseTypeEntityMapper mapper;

    @Override
    public Page<DoseTypeDTO> findAllByMedicationTypeId(@NotNull String medicationTypeId, @NotNull Pageable pageable) {
        return repository.findAllByMedicationTypeId(medicationTypeId, pageable).map(mapper::toDTO);
    }
}
