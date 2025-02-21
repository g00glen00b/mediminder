package codes.dimitri.mediminder.api.user;

import java.time.ZoneId;
import java.util.UUID;

public record UserDTO(
    UUID id,
    String name,
    ZoneId timezone,
    boolean enabled,
    boolean admin
) {
}
