package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.notification.NotificationType;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationExistenceProcessor implements ItemProcessor<NotificationEntity, NotificationEntity> {
    private final NotificationEntityRepository repository;

    @Override
    public NotificationEntity process(NotificationEntity item) {
        NotificationType type = item.getType();
        UUID initiatorId = item.getInitiatorId();
        UUID userId = item.getUserId();
        if (repository.existsByUserIdAndTypeAndInitiatorId(userId, type, initiatorId)) return null;
        else return item;
    }
}
