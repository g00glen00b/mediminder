package codes.dimitri.mediminder.api.notification.implementation.batch;

import java.util.List;
import java.util.Map;

record PushNotificationPayload(String title, String body, List<PushNotificationAction> actions, PushNotificationData data) {
    public static PushNotificationPayloadWrapper simple(String title, String body) {
        PushNotificationOperation operation = new PushNotificationOperation("openWindow", "/");
        PushNotificationData data = new PushNotificationData(Map.of("default", operation));
        PushNotificationPayload payload = new PushNotificationPayload(title, body, List.of(), data);
        return new PushNotificationPayloadWrapper(payload);
    }
}
