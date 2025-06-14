package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.schedule.EventDTO;
import codes.dimitri.mediminder.api.schedule.EventManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
class EventTool implements AssistantTool {
    private final EventManager manager;

    @Tool(description = "get all medication to be taken by the user on a given date")
    List<EventDTO> findAllEventsByDate(
        @ToolParam(description = "given date in ISO-8601 format (YYYY-MM-DD)") String date) {
        log.debug("Called findAllEventsByDate with date: {}", date);
        return manager.findAll(LocalDate.parse(date));
    }

    @Tool(description = "take a medication")
    void takeMedication(
        @ToolParam(description = "UUID of the schedule") String scheduleId,
        @ToolParam(description = "date at which the medication was taken in ISO-8601 format (YYYY-MM-DD)") String date) {
        log.debug("Called takeMedication with scheduleId: {}, date: {}", scheduleId, date);
        manager.complete(UUID.fromString(scheduleId), LocalDate.parse(date));
    }
}
