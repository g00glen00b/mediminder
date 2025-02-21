package codes.dimitri.mediminder.api.schedule;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ScheduleNotFoundException extends RuntimeException {
    private final UUID id;

    @Override
    public String getMessage() {
        return "Schedule with ID '" + id + "' does not exist";
    }
}
