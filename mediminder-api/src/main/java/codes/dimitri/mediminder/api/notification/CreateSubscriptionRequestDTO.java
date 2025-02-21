package codes.dimitri.mediminder.api.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSubscriptionRequestDTO(
    @NotBlank(message = "Endpoint is required")
    @Size(max = 256, message = "Endpoint cannot contain more than {max} characters")
    String endpoint,
    @Valid
    @NotNull(message = "Keys is required")
    SubscriptionKeysDTO keys) {
}
