package codes.dimitri.mediminder.api.notification;

import java.util.UUID;

public record NotificationDTO(UUID id, NotificationType type, String title, String message) {
}
