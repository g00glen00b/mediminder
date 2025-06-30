package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationEntityRepository extends JpaRepository<NotificationEntity, UUID> {
    boolean existsByUserIdAndTypeAndInitiatorId(String userId, NotificationType type, UUID initiatorId);
    @Query("""
    select n from NotificationEntity n
    where n.active = true
    and n.userId = ?1
    """)
    Page<NotificationEntity> findAllActiveByUserId(String userId, Pageable pageable);
    Optional<NotificationEntity> findByIdAndUserId(UUID id, String userId);

    @Modifying
    @Query("""
    delete from NotificationEntity n
    where n.deleteAt <= ?1
    """)
    void deleteAllByDeleteAtBefore(Instant date);

    @Modifying
    @Query("""
    update NotificationEntity n
    set n.active = false
    where n.userId = ?1
    and n.type = ?2
    and n.initiatorId = ?3
    """)
    void deactivateAllByUserIdTypeAndInitiatorId(String userId, NotificationType type, UUID initiatorId);

}
