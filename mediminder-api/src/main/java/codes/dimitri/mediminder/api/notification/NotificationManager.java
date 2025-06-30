package codes.dimitri.mediminder.api.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface NotificationManager {
    @Transactional
    void subscribe(@Valid @NotNull CreateSubscriptionRequestDTO request);

    @Transactional
    void unsubscribe();

    Page<NotificationDTO> findAll(@NotNull Pageable pageable);

    @Transactional
    void delete(@NotNull UUID id);

    @Transactional
    void deleteAllByUserIdTypeAndInitiatorId(
        @NotNull String userId,
        @NotNull NotificationType type,
        @NotNull UUID initiatorId);

    SubscriptionConfigurationDTO findConfiguration();
}
