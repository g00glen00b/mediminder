package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.UpdateUserRequestDTO;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;

import static codes.dimitri.mediminder.api.common.ValidationUtilities.getAnyConstraintViolation;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
class UserController {
    private final UserManager manager;

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

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @SecurityRequirements
    @DeleteMapping
    public void deleteCurrentUser(HttpServletRequest request) {
        manager.deleteCurrentUser();
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
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
}
