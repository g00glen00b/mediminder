package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.*;
import codes.dimitri.mediminder.api.user.implementation.cleanup.UserCodeCleanupBatchTask;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static codes.dimitri.mediminder.api.common.ValidationUtilities.getAnyConstraintViolation;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
class UserController {
    private final UserManager manager;
    private final UserCodeCleanupBatchTask task;

    @SecurityRequirements
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserDTO register(@RequestBody RegisterUserRequestDTO request) {
        return manager.register(request);
    }

    @SecurityRequirements
    @PostMapping("/verify")
    public UserDTO verify(@RequestParam String code) {
        return manager.verify(code);
    }

    @SecurityRequirements
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/verify/reset")
    public void resetVerification(
        @RequestParam
        String email) {
        manager.resetVerification(email);
    }

    @GetMapping("/current")
    public UserDTO findCurrentUser() {
        return manager.findCurrentUser();
    }

    @SecurityRequirements
    @GetMapping("/timezone")
    public Collection<String> findAvailableTimezones(@RequestParam(required = false) String search) {
        return manager.findAvailableTimezones(search);
    }

    @PutMapping
    public UserDTO update(@RequestBody UpdateUserRequestDTO request) {
        return manager.update(request);
    }

    @PutMapping("/credentials")
    public UserDTO updateCredentials(@RequestBody UpdateCredentialsRequestDTO request) {
        return manager.updateCredentials(request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirements
    @PostMapping("/credentials/reset/request")
    public void requestResetCredentials(
        @RequestParam
        @NotBlank(message = "E-mail is required")
        @Email(message = "E-mail must be a valid e-mail address")
        String email) {
        manager.requestResetCredentials(email);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirements
    @PostMapping("/credentials/reset/confirm")
    public void confirmResetCredentials(@RequestBody ResetCredentialsRequestDTO request) {
        manager.resetCredentials(request);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/batch/unused-code/start")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CompletableFuture<Void> launchCodeCleanupJob() {
        return CompletableFuture.runAsync(task);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirements
    @DeleteMapping
    public void deleteCurrentUser(HttpServletRequest request) {
        manager.deleteCurrentUser();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    @ExceptionHandler(InvalidUserException.class)
    public ErrorResponse handleInvalidUser(InvalidUserException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage())
            .title("Invalid user")
            .type(URI.create("https://mediminder/user/invalid"))
            .build();
    }

    @ExceptionHandler(UserMailFailedException.class)
    public ErrorResponse handleMailFailed(UserMailFailedException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage())
            .title("Sending mail failed")
            .type(URI.create("https://mediminder/user/internal/mail"))
            .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        String detail = getAnyConstraintViolation(ex)
            .map(ConstraintViolation::getMessage)
            .orElse("Validation failed");
        return ErrorResponse
            .builder(ex, HttpStatus.BAD_REQUEST, detail)
            .title("Invalid user")
            .type(URI.create("https://mediminder/user/invalid"))
            .build();
    }

    @ExceptionHandler(UserCodeGenerationException.class)
    public ErrorResponse handleCodeGenerationException(UserCodeGenerationException ex) {
        return ErrorResponse
            .builder(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage())
            .title("Generating unique code for user failed")
            .type(URI.create("https://mediminder/user/internal/code"))
            .build();
    }
}
