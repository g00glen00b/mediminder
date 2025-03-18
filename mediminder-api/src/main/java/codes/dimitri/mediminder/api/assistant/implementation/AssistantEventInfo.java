package codes.dimitri.mediminder.api.assistant.implementation;

import java.time.LocalTime;

public record AssistantEventInfo(
    LocalTime targetTime,
    LocalTime completedTime
) {
}
