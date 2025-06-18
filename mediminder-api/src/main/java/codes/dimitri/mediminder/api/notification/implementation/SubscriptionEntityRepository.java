package codes.dimitri.mediminder.api.notification.implementation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionEntityRepository extends JpaRepository<SubscriptionEntity, String> {
}
