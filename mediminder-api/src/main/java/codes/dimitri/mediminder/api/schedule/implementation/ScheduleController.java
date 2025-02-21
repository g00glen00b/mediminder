package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.schedule.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
class ScheduleController {
    private final ScheduleManager manager;

    @GetMapping
    public Page<ScheduleDTO> findAll(@ParameterObject Pageable pageable) {
        return manager.findAllForCurrentUser(pageable);
    }

    @PostMapping
    public ScheduleDTO create(@RequestBody CreateScheduleRequestDTO request) {
        return manager.createForCurrentUser(request);
    }

    @PutMapping("/{id}")
    public ScheduleDTO update(@PathVariable UUID id, @RequestBody UpdateScheduleRequestDTO request) {
        return manager.updateForCurrentUser(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        manager.deleteForCurrentUser(id);
    }

    @GetMapping("/{id}")
    public ScheduleDTO findById(@PathVariable UUID id) {
        return manager.findByIdForCurrentUser(id);
    }

    @ExceptionHandler(ScheduleNotFoundException.class)
    public ErrorResponse handleNotFound(ScheduleNotFoundException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.NOT_FOUND, ex.getMessage())
            .title("Schedule not found")
            .type(URI.create("https://mediminder/schedule/notfound"))
            .build();
    }

    @ExceptionHandler(InvalidScheduleException.class)
    public ErrorResponse handleInvalid(InvalidScheduleException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
            .title("Invalid schedule")
            .type(URI.create("https://mediminder/schedule/invalid"))
            .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        String detail = getAnyConstraintViolation(ex)
            .map(ConstraintViolation::getMessage)
            .orElse("Validation failed");
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, detail)
            .title("Invalid schedule")
            .type(URI.create("https://mediminder/schedule/invalid"))
            .build();
    }

    private static Optional<ConstraintViolation<?>> getAnyConstraintViolation(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream().findAny();
    }
}
