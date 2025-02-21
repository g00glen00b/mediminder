package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.AdministrationTypeDTO;
import codes.dimitri.mediminder.api.medication.AdministrationTypeManager;
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
class AdministrationTypeManagerImpl implements AdministrationTypeManager {
    private final AdministrationTypeEntityRepository repository;
    private final AdministrationTypeEntityMapper mapper;

    @Override
    public Page<AdministrationTypeDTO> findAllByMedicationTypeId(@NotNull String medicationTypeId, @NotNull Pageable pageable) {
        return repository.findAllByMedicationTypeId(medicationTypeId, pageable).map(mapper::toDTO);
    }
}
