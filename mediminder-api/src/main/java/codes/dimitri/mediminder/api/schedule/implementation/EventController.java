package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.schedule.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static codes.dimitri.mediminder.api.common.ValidationUtilities.getAnyConstraintViolation;

@RestController
@RequiredArgsConstructor
class EventController {
    private final EventManager manager;

    @GetMapping("/api/event/{targetDate}")
    public List<EventDTO> findAll(@PathVariable LocalDate targetDate) {
        return manager.findAll(targetDate);
    };

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/schedule/{scheduleId}/event/{targetDate}")
    public EventDTO complete(@PathVariable UUID scheduleId, @PathVariable LocalDate targetDate) {
        return manager.complete(scheduleId, targetDate);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/event/{eventId}")
    public void delete(@PathVariable UUID eventId) {
        manager.uncomplete(eventId);
    }

    @ExceptionHandler({EventNotFoundException.class, CompletedEventNotFoundException.class})
    public ErrorResponse handleNotFound(RuntimeException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.NOT_FOUND, ex.getMessage())
            .title("Event not found")
            .type(URI.create("https://mediminder/event/notfound"))
            .build();
    }

    @ExceptionHandler(InvalidEventException.class)
    public ErrorResponse handleInvalid(InvalidEventException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
            .title("Invalid event")
            .type(URI.create("https://mediminder/event/invalid"))
            .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        String detail = getAnyConstraintViolation(ex)
            .map(ConstraintViolation::getMessage)
            .orElse("Validation failed");
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, detail)
            .title("Invalid event")
            .type(URI.create("https://mediminder/event/invalid"))
            .build();
    }
}
