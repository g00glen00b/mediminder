package codes.dimitri.mediminder.api.user;

import jakarta.validation.constraints.NotBlank;

public record ResetCredentialsRequestDTO(
    @NotBlank(message = "Password reset code is required")
    String passwordResetCode,
    @NotBlank(message = "New password is required")
    String newPassword
) {
}
