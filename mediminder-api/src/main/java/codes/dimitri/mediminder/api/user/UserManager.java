package codes.dimitri.mediminder.api.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface UserManager {

    Optional<UserDTO> findById(@NotNull UUID id);

    Optional<UserDTO> findCurrentUser();

    @Transactional
    UserDTO register(@Valid RegisterUserRequestDTO request);

    @Transactional
    UserDTO verify(@NotNull String verificationCode);

    Collection<String> findAvailableTimezones(String search);

    @Transactional
    void resetVerification(
        @NotBlank(message = "E-mail is required")
        @Email(message = "E-mail must be a valid e-mail address")
        String email);

    LocalDateTime calculateTodayForUser(UUID id);

    @Transactional
    UserDTO update(@Valid UpdateUserRequestDTO request);

    @Transactional
    UserDTO updateCredentials(@Valid UpdateCredentialsRequestDTO request);

    @Transactional
    void requestResetCredentials(
        @NotBlank(message = "E-mail is required")
        @Email(message = "E-mail must be a valid e-mail address")
        String email);

    @Transactional
    void resetCredentials(@Valid ResetCredentialsRequestDTO request);
}
