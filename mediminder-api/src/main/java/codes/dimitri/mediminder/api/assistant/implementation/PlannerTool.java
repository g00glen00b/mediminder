package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.planner.MedicationPlannerDTO;
import codes.dimitri.mediminder.api.planner.PlannerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlannerTool implements AssistantTool {
    private final PlannerManager manager;
    private final AssistantProperties properties;

    @Tool(description = "get all required doses until a given date (YYYY-MM-DD)")
    public List<MedicationPlannerDTO> findAll(String targetDate) {
        return manager.findAll(LocalDate.parse(targetDate), PageRequest.ofSize(properties.maxSize())).getContent();
    }
}
