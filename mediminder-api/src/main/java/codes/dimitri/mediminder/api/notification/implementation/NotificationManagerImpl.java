package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.notification.*;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
class NotificationManagerImpl implements NotificationManager {
    private final UserManager userManager;
    private final NotificationEntityRepository repository;
    private final SubscriptionEntityRepository subscriptionRepository;
    private final NotificationMapper mapper;
    private final NotificationProperties properties;

    @Override
    @Transactional
    public void subscribe(@Valid @NotNull CreateSubscriptionRequestDTO request) {
        UserDTO user = findCurrentUser();
        subscriptionRepository.findById(user.id())
            .ifPresentOrElse(
                entity -> updateSubscription(request, entity),
                () -> createSubscription(request, user));
    }

    @Override
    @Transactional
    public void unsubscribe() {
        UserDTO user = findCurrentUser();
        subscriptionRepository.deleteById(user.id());
    }

    private static void updateSubscription(CreateSubscriptionRequestDTO request, SubscriptionEntity entity) {
        entity.setAuth(request.keys().auth());
        entity.setEndpoint(request.endpoint());
        entity.setKey(request.keys().p256dh());
    }

    private void createSubscription(CreateSubscriptionRequestDTO request, UserDTO user) {
        SubscriptionEntity entity = new SubscriptionEntity(
            user.id(),
            request.endpoint(),
            request.keys().p256dh(),
            request.keys().auth()
        );
        subscriptionRepository.save(entity);
    }

    @Override
    public Page<NotificationDTO> findAll(@NotNull Pageable pageable) {
        UserDTO user = findCurrentUser();
        return repository
            .findAllActiveByUserId(user.id(), pageable)
            .map(mapper::toDTO);
    }

    @Override
    @Transactional
    public void delete(@NotNull UUID id) {
        UserDTO user = findCurrentUser();
        NotificationEntity entity = findEntity(id, user);
        entity.setActive(false);
    }

    @Transactional
    @Override
    public void deleteAllByUserIdTypeAndInitiatorId(
        @NotNull String userId,
        @NotNull NotificationType type,
        @NotNull UUID initiatorId) {
        repository.deactivateAllByUserIdTypeAndInitiatorId(userId, type, initiatorId);
    }

    private UserDTO findCurrentUser() {
        try {
            return userManager.findCurrentUser();
        } catch (CurrentUserNotFoundException ex) {
            throw new InvalidNotificationException("User is not authenticated", ex);
        }
    }

    private NotificationEntity findEntity(UUID id, UserDTO currentUser) {
        return repository
            .findByIdAndUserId(id, currentUser.id())
            .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    @Override
    public SubscriptionConfigurationDTO findConfiguration() {
        return new SubscriptionConfigurationDTO(properties.publicKey());
    }
}
