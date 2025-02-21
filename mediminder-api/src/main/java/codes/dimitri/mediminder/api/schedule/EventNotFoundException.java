package codes.dimitri.mediminder.api.schedule;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
public class EventNotFoundException extends RuntimeException {
    private final UUID scheduleId;
    private final LocalDate targetDate;

    @Override
    public String getMessage() {
        return "Schedule '" + scheduleId + "' is not expected to be taken at '" + DateTimeFormatter.ISO_LOCAL_DATE.format(targetDate) + "'";
    }
}
