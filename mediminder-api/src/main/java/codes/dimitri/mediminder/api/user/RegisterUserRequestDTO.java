package codes.dimitri.mediminder.api.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.ZoneId;

public record RegisterUserRequestDTO(
    @Email(message = "E-mail address '${validatedValue}' is not a valid e-mail address")
    @NotBlank(message = "E-mail address is required")
    @Size(max = 128, message = "E-mail address should not contain more than {max} characters")
    String email,
    @NotBlank(message = "Password is required")
    String password,
    @Size(max = 128, message = "Name should not contain more than {max} characters")
    String name,
    ZoneId timezone
) {
}
