package codes.dimitri.mediminder.api.schedule;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
public class CompletedEventNotFoundException extends RuntimeException {
    private final UUID id;

    @Override
    public String getMessage() {
        return "Completed event with ID '" + id + "' does not exist";
    }
}
