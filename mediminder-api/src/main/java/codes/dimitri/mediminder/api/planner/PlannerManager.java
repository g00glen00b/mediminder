package codes.dimitri.mediminder.api.planner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PlannerManager {
    Page<MedicationPlannerDTO> findAll(LocalDate targetDate, Pageable pageable);
}
