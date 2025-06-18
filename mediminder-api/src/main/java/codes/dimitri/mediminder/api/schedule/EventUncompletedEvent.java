package codes.dimitri.mediminder.api.schedule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventUncompletedEvent(
    UUID id,
    String userId,
    UUID scheduleId,
    UUID medicationId,
    LocalDateTime targetDate,
    LocalDateTime completedDate,
    BigDecimal dose) {
}
