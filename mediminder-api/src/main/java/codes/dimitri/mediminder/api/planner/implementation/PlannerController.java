package codes.dimitri.mediminder.api.planner.implementation;

import codes.dimitri.mediminder.api.planner.MedicationPlannerDTO;
import codes.dimitri.mediminder.api.planner.PlannerManager;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/planner")
@RequiredArgsConstructor
class PlannerController {
    private final PlannerManager manager;

    @GetMapping("/{targetDate}")
    public Page<MedicationPlannerDTO> findAll(@PathVariable LocalDate targetDate, @ParameterObject Pageable pageable) {
        return manager.findAll(targetDate, pageable);
    }
}
