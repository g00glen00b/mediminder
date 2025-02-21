package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.MedicationTypeDTO;
import codes.dimitri.mediminder.api.medication.MedicationTypeManager;
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
class MedicationTypeManagerImpl implements MedicationTypeManager {
    private final MedicationTypeEntityRepository repository;
    private final MedicationTypeEntityMapper mapper;

    @Override
    public Page<MedicationTypeDTO> findAll(@NotNull Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }
}
