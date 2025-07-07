package codes.dimitri.mediminder.api.notification.implementation.batch;

import java.util.List;
import java.util.Map;
import java.util.UUID;

record PushNotificationPayload(String title, String body, String icon, List<PushNotificationAction> actions, PushNotificationData data) {
    public static PushNotificationPayloadWrapper simple(String title, String body, String icon) {
        PushNotificationOperation operation = new PushNotificationOperation("openWindow", "/");
        PushNotificationData data = new PushNotificationData(Map.of("default", operation));
        PushNotificationPayload payload = new PushNotificationPayload(title, body, icon, List.of(), data);
        return new PushNotificationPayloadWrapper(payload);
    }

    public static PushNotificationPayloadWrapper completable(String title, String body, String icon, UUID notificationId, String completeLabel) {
        PushNotificationOperation openOperation = new PushNotificationOperation("openWindow", "/");
        PushNotificationOperation completeOperation = new PushNotificationOperation("openWindow", "/home?complete=" + notificationId.toString());
        PushNotificationAction completeAction = new PushNotificationAction("notification.complete", completeLabel);
        PushNotificationData data = new PushNotificationData(Map.of(
            "default", openOperation,
            "notification.complete", completeOperation
        ));
        PushNotificationPayload payload = new PushNotificationPayload(title, body, icon, List.of(completeAction), data);
        return new PushNotificationPayloadWrapper(payload);
    }
}
