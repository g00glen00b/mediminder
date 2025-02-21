package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntityRepository;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.stereotype.Component;

@Component
public class NotificationItemWriter extends RepositoryItemWriter<NotificationEntity> {
    public NotificationItemWriter(NotificationEntityRepository repository) {
        this.setRepository(repository);
    }
}
