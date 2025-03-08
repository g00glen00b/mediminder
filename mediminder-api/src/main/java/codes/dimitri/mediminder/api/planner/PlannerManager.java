package codes.dimitri.mediminder.api.planner;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PlannerManager {
    Page<MedicationPlannerDTO> findAll(@NotNull LocalDate targetDate, @NotNull Pageable pageable);
}
