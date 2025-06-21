package codes.dimitri.mediminder.api.user;

import java.time.ZoneId;

public record UserDTO(
    String id,
    String name,
    ZoneId timezone
) {
}
