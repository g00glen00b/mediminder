package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.notification.*;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(
    classes = {NotificationManagerImpl.class, NotificationMapperImpl.class},
    properties = {
        "notification.public-key=publickey",
        "notification.private-key=privatekey"
    })
@ContextConfiguration(classes = ValidationAutoConfiguration.class)
@EnableConfigurationProperties(NotificationProperties.class)
class NotificationManagerImplTest {
    @Autowired
    private NotificationManagerImpl manager;
    @MockBean
    private NotificationEntityRepository repository;
    @MockBean
    private SubscriptionEntityRepository subscriptionRepository;
    @MockBean
    private UserManager userManager;
    @Captor
    private ArgumentCaptor<SubscriptionEntity> anySubscriptionEntity;

    @Nested
    class subscribe {
        @Test
        void createsEntity() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var request = Instancio.create(CreateSubscriptionRequestDTO.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            manager.subscribe(request);
            verify(subscriptionRepository).save(anySubscriptionEntity.capture());
            assertThat(anySubscriptionEntity.getValue())
                .usingRecursiveComparison()
                .isEqualTo(new SubscriptionEntity(
                    user.id(),
                    request.endpoint(),
                    request.keys().p256dh(),
                    request.keys().auth()
                ));
        }

        @Test
        void updatesEntity() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var request = Instancio.create(CreateSubscriptionRequestDTO.class);
            var entity = Instancio.of(SubscriptionEntity.class)
                .set(field(SubscriptionEntity::getUserId), user.id())
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(subscriptionRepository.findById(any())).thenReturn(Optional.of(entity));
            // Then
            manager.subscribe(request);
            verify(subscriptionRepository).findById(user.id());
            verifyNoMoreInteractions(subscriptionRepository);
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new SubscriptionEntity(
                    user.id(),
                    request.endpoint(),
                    request.keys().p256dh(),
                    request.keys().auth()
                ));
        }

        @Test
        void throwsExceptionIfUserNotFound() {
            // Given
            var request = Instancio.create(CreateSubscriptionRequestDTO.class);
            // Then
            assertThatExceptionOfType(InvalidNotificationException.class)
                .isThrownBy(() -> manager.subscribe(request))
                .withMessage("User is not authenticated");
        }

        @Test
        void throwsExceptionEndpointNotGiven() {
            // Given
            var request = Instancio.of(CreateSubscriptionRequestDTO.class)
                .ignore(field(CreateSubscriptionRequestDTO::endpoint))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.subscribe(request))
                .withMessageEndingWith("Endpoint is required");
        }

        @Test
        void throwsExceptionEndpointTooLong() {
            // Given
            var request = Instancio.of(CreateSubscriptionRequestDTO.class)
                .generate(field(CreateSubscriptionRequestDTO::endpoint), gen -> gen.string().length(257))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.subscribe(request))
                .withMessageEndingWith("Endpoint cannot contain more than 256 characters");
        }

        @Test
        void throwsExceptionIfKeysNotGiven() {
            // Given
            var request = Instancio.of(CreateSubscriptionRequestDTO.class)
                .ignore(field(CreateSubscriptionRequestDTO::keys))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.subscribe(request))
                .withMessageEndingWith("Keys is required");
        }

        @Test
        void throwsExceptionIfp256dhNotGiven() {
            // Given
            var request = Instancio.of(CreateSubscriptionRequestDTO.class)
                .ignore(field(SubscriptionKeysDTO::p256dh))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.subscribe(request))
                .withMessageEndingWith("Key is required");
        }

        @Test
        void throwsExceptionIfAuthNotGiven() {
            // Given
            var request = Instancio.of(CreateSubscriptionRequestDTO.class)
                .ignore(field(SubscriptionKeysDTO::auth))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.subscribe(request))
                .withMessageEndingWith("Auth is required");
        }

        @Test
        void throwsExceptionP256dhTooLong() {
            // Given
            var request = Instancio.of(CreateSubscriptionRequestDTO.class)
                .generate(field(SubscriptionKeysDTO::p256dh), gen -> gen.string().length(257))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.subscribe(request))
                .withMessageEndingWith("Key cannot contain more than 256 characters");
        }

        @Test
        void throwsExceptionIfAuthTooLong() {
            // Given
            var request = Instancio.of(CreateSubscriptionRequestDTO.class)
                .generate(field(SubscriptionKeysDTO::auth), gen -> gen.string().length(257))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.subscribe(request))
                .withMessageEndingWith("Auth cannot contain more than 256 characters");
        }
    }

    @Nested
    class unsubscribe {
        @Test
        void deletesEntity() {
            // Given
            var user = Instancio.create(UserDTO.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            manager.unsubscribe();
            verify(subscriptionRepository).deleteById(user.id());
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            assertThatExceptionOfType(InvalidNotificationException.class)
                .isThrownBy(() -> manager.unsubscribe())
                .withMessage("User is not authenticated");
        }
    }

    @Nested
    class findAll {
        @Test
        void returnsNotifications() {
            // Given
            var pageRequest = PageRequest.of(0, 20);
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(NotificationEntity.class)
                .set(field(NotificationEntity::getUserId), user.id())
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(repository.findAllActiveByUserId(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));
            // Then
            Page<NotificationDTO> result = manager.findAll(pageRequest);
            assertThat(result).containsOnly(new NotificationDTO(
               entity.getId(),
               entity.getType(),
               entity.getTitle(),
               entity.getMessage()
            ));
            verify(repository).findAllActiveByUserId(user.id(), pageRequest);
            verify(userManager).findCurrentUser();
        }

        @Test
        void throwsExceptionWhenUserNotFound() {
            // Given
            var pageRequest = PageRequest.of(0, 20);
            // Then
            assertThatExceptionOfType(InvalidNotificationException.class)
                .isThrownBy(() -> manager.findAll(pageRequest))
                .withMessage("User is not authenticated");
            verifyNoInteractions(repository);
        }
    }

    @Nested
    class delete {
        @Test
        void setsEntityInactive() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(NotificationEntity.class)
                .set(field(NotificationEntity::getUserId), user.id())
                .ignore(field(NotificationEntity::isActive))
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            // Then
            manager.delete(entity.getId());
            verify(repository).findByIdAndUserId(entity.getId(), user.id());
            verify(userManager).findCurrentUser();
            assertThat(entity.isActive()).isFalse();
        }

        @Test
        void throwsExceptionIfEntityNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var id = UUID.randomUUID();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(NotificationNotFoundException.class)
                .isThrownBy(() -> manager.delete(id))
                .withMessage("Notification with ID '" + id + "' does not exist");
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var id = UUID.randomUUID();
            // Then
            assertThatExceptionOfType(InvalidNotificationException.class)
                .isThrownBy(() -> manager.delete(id))
                .withMessage("User is not authenticated");
            verifyNoInteractions(repository);
        }
    }

    @Nested
    class findConfiguration {
        @Test
        void returnsPublicKey() {
            SubscriptionConfigurationDTO configuration = manager.findConfiguration();
            assertThat(configuration).isEqualTo(new SubscriptionConfigurationDTO(
               "publickey"
            ));
        }
    }
}