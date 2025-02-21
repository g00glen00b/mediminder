package codes.dimitri.mediminder.api.notification.implementation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionEntityRepository extends JpaRepository<SubscriptionEntity, UUID> {
}
