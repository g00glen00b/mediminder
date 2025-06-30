package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationProperties;
import codes.dimitri.mediminder.api.notification.implementation.SubscriptionEntity;
import codes.dimitri.mediminder.api.notification.implementation.SubscriptionEntityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.jose4j.lang.JoseException;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationWriter implements ItemWriter<NotificationEntity> {
    private final NotificationProperties properties;
    private final PushService pushService;
    private final SubscriptionEntityRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public void write(Chunk<? extends NotificationEntity> chunk) {
        chunk.forEach(this::sendPushNotification);
    }

    private void sendPushNotification(NotificationEntity entity) {
        Optional<SubscriptionEntity> optionalSubscription = repository.findById(entity.getUserId());
        if (optionalSubscription.isEmpty()) return;
        var subscriptionEntity = optionalSubscription.get();
        Subscription.Keys keys = new Subscription.Keys(subscriptionEntity.getKey(), subscriptionEntity.getAuth());
        Subscription subscription = new Subscription(subscriptionEntity.getEndpoint(), keys);
        try {
            PushNotificationPayloadWrapper pushNotification = PushNotificationPayload.simple(
                entity.getTitle(),
                entity.getMessage(),
                properties.applicationIconUrl()
            );
            String jsonPayload = objectMapper.writeValueAsString(pushNotification);
            Notification notification = new Notification(subscription, jsonPayload);
            pushService.send(notification);
        } catch (GeneralSecurityException | IOException | ExecutionException | InterruptedException | JoseException ex) {
            log.error("Could not deliver push notification for {}", entity.getId(), ex);
        }
    }
}
