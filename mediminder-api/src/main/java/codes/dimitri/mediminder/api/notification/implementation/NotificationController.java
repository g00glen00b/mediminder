package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.notification.*;
import codes.dimitri.mediminder.api.notification.implementation.batch.NotificationBatchTask;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static codes.dimitri.mediminder.api.common.ValidationUtilities.getAnyConstraintViolation;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationManager manager;
    private final NotificationBatchTask task;

    @GetMapping
    public Page<NotificationDTO> findAll(@ParameterObject Pageable pageable) {
        return manager.findAll(pageable);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        manager.delete(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/subscription")
    public void subscribe(@RequestBody CreateSubscriptionRequestDTO request) {
        manager.subscribe(request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/subscription")
    public void unsubscribe() {
        manager.unsubscribe();
    }

    @GetMapping("/subscription/configuration")
    public SubscriptionConfigurationDTO findConfiguration() {
        return manager.findConfiguration();
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/batch/start")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CompletableFuture<Void> launchJob() {
        return CompletableFuture.runAsync(task);
    }

    @ExceptionHandler(InvalidNotificationException.class)
    public ErrorResponse handleInvalid(InvalidNotificationException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
            .title("Invalid notification")
            .type(URI.create("https://mediminder/notification/invalid"))
            .build();
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ErrorResponse handleNotFound(NotificationNotFoundException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.NOT_FOUND, ex.getMessage())
            .title("Notification not found")
            .type(URI.create("https://mediminder/notification/notfound"))
            .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        String detail = getAnyConstraintViolation(ex)
            .map(ConstraintViolation::getMessage)
            .orElse("Validation failed");
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, detail)
            .title("Invalid notification")
            .type(URI.create("https://mediminder/notification/invalid"))
            .build();
    }
}
