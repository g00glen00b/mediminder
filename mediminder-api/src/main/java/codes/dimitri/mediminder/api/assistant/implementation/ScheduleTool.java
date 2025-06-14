package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.schedule.ScheduleDTO;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class ScheduleTool implements AssistantTool {
    private final ScheduleManager manager;
    private final AssistantProperties properties;

    @Tool(description = "get all schedules for a given medication ID")
    List<ScheduleDTO> findAllSchedulesByMedicationId(String medicationId) {
        log.debug("Called findAllSchedulesByMedicationId with medicationId: {}", medicationId);
        return manager
            .findAllForCurrentUser(UUID.fromString(medicationId), false, PageRequest.ofSize(properties.maxSize()))
            .getContent();
    }
}
