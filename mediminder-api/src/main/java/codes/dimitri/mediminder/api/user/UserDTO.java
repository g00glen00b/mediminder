package codes.dimitri.mediminder.api.user;

import java.time.ZoneId;
import java.util.List;

public record UserDTO(
    String id,
    String name,
    ZoneId timezone,
    List<String> authorities
) {
    public UserDTO(String id, String name, ZoneId timezone) {
        this(id, name, timezone, List.of());
    }
}
