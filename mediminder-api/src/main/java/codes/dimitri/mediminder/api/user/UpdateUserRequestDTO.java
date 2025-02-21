package codes.dimitri.mediminder.api.user;

import jakarta.validation.constraints.Size;

import java.time.ZoneId;

public record UpdateUserRequestDTO(
    @Size(max = 128, message = "Name should not contain more than {max} characters")
    String name,
    ZoneId timezone
) {
}
