package codes.dimitri.mediminder.api.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateCredentialsRequestDTO(
    @NotBlank(message = "Original password is required")
    String oldPassword,
    @NotBlank(message = "New password is required")
    String newPassword
) {
}
