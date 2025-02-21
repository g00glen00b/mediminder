package codes.dimitri.mediminder.api.medication;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DoseTypeManager {
    Page<DoseTypeDTO> findAllByMedicationTypeId(@NotNull String medicationTypeId, @NotNull Pageable pageable);
}
