package codes.dimitri.mediminder.api.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubscriptionKeysDTO(
    @NotBlank(message = "Key is required")
    @Size(max = 256, message = "Key cannot contain more than {max} characters")
    String p256dh,
    @NotBlank(message = "Auth is required")
    @Size(max = 256, message = "Auth cannot contain more than {max} characters")
    String auth) {
}
