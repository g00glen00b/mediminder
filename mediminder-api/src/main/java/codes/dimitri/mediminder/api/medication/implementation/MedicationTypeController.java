package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.*;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medication-type")
@RequiredArgsConstructor
class MedicationTypeController {
    private final MedicationTypeManager typeManager;
    private final DoseTypeManager doseTypeManager;
    private final AdministrationTypeManager administrationTypeManager;

    @GetMapping
    public Page<MedicationTypeDTO> findAll(@ParameterObject Pageable pageable) {
        return typeManager.findAll(pageable);
    }

    @GetMapping("/{id}/dose-type")
    public Page<DoseTypeDTO> findAllDoseTypes(@PathVariable String id, @ParameterObject Pageable pageable) {
        return doseTypeManager.findAllByMedicationTypeId(id, pageable);
    }

    @GetMapping("/{id}/administration-type")
    public Page<AdministrationTypeDTO> findAllAdministrationTypes(@PathVariable String id, @ParameterObject Pageable pageable) {
        return administrationTypeManager.findAllByMedicationTypeId(id, pageable);
    }
}
