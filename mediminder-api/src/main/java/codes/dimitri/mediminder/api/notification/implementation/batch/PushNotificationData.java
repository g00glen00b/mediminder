package codes.dimitri.mediminder.api.notification.implementation.batch;

import java.util.Map;

record PushNotificationData(Map<String, PushNotificationOperation> onActionClick) {
}
