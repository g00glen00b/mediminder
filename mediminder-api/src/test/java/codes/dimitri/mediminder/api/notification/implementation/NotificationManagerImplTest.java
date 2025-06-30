package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.document.DocumentManager;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.notification.*;
import codes.dimitri.mediminder.api.schedule.EventManager;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.shared.TestClockConfiguration;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "notification.public-key=BIyq6YYFYOCttqL-N22xS84_EfO2CFYhn86ZW4gkzIK_uTht7rofUlIrXpu_r4-BT-qmf2TZFAq92jKhcBFIF-w",
    "notification.private-key=CX5aOzJFXYQszpj__Trqa9GOIupZMLRrubTxsc3zNg0",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2"
})
@Import({
    TestClockConfiguration.class
})
@Transactional
@Sql({"classpath:test-data/subscriptions.sql", "classpath:test-data/notification.sql"})
@Sql(value = {
    "classpath:test-data/cleanup-subscriptions.sql",
    "classpath:test-data/cleanup-notification.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class NotificationManagerImplTest {
    @Autowired
    private NotificationManager notificationManager;
    @MockitoBean
    private UserManager userManager;
    @MockitoBean
    private ScheduleManager scheduleManager;
    @MockitoBean
    private MedicationManager medicationManager;
    @MockitoBean
    private CabinetEntryManager cabinetEntryManager;
    @MockitoBean
    private DocumentManager documentManager;
    @MockitoBean
    private EventManager eventManager;
    @Autowired
    private SubscriptionEntityRepository subscriptionRepository;
    @Autowired
    private NotificationEntityRepository notificationRepository;

    @Nested
    class subscribe {
        @Test
        void createsSubscription() {
            var user = new UserDTO(
                "auth|ff9d85fcc3c505949092c",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var request = new CreateSubscriptionRequestDTO(
                "https://example.org/3",
                new SubscriptionKeysDTO(
                    "key3",
                    "auth3"
                )
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            notificationManager.subscribe(request);
            List<SubscriptionEntity> results = subscriptionRepository.findAll();
            assertThat(results)
                .hasSize(3)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(new SubscriptionEntity(
                    user.id(),
                    "https://example.org/3",
                    "key3",
                    "auth3"
                ));
        }

        @Test
        void updatesSubscriptionIfExisting() {
            var user = new UserDTO(
                "auth|3743a8f73e0d4c06baa4",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var request = new CreateSubscriptionRequestDTO(
                "https://example.org/3",
                new SubscriptionKeysDTO(
                    "key3",
                    "auth3"
                )
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            notificationManager.subscribe(request);
            List<SubscriptionEntity> results = subscriptionRepository.findAll();
            assertThat(results)
                .hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(new SubscriptionEntity(
                    user.id(),
                    "https://example.org/3",
                    "key3",
                    "auth3"
                ));
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var request = new CreateSubscriptionRequestDTO(
                "https://example.org/3",
                new SubscriptionKeysDTO(
                    "key3",
                    "auth3"
                )
            );
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidNotificationException.class)
                .isThrownBy(() -> notificationManager.subscribe(request))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfRequestNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> notificationManager.subscribe(null));
        }

        @Test
        void failsIfEndpointMissing() {
            var request = new CreateSubscriptionRequestDTO(
                null,
                new SubscriptionKeysDTO(
                    "key3",
                    "auth3"
                )
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> notificationManager.subscribe(request))
                .withMessageContaining("Endpoint is required");
        }

        @Test
        void failsIfEndpointMoreThan256Characters() {
            var request = new CreateSubscriptionRequestDTO(
                "a".repeat(257),
                new SubscriptionKeysDTO(
                    "key3",
                    "auth3"
                )
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> notificationManager.subscribe(request))
                .withMessageContaining("Endpoint cannot contain more than 256 characters");
        }

        @Test
        void failsIfKeysIsMissing() {
            var request = new CreateSubscriptionRequestDTO(
                "https://example.org/3",
                null
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> notificationManager.subscribe(request))
                .withMessageContaining("Keys is required");
        }

        @Test
        void failsIfAuthIsMissing() {
            var request = new CreateSubscriptionRequestDTO(
                "https://example.org/3",
                new SubscriptionKeysDTO(
                    "key3",
                    null
                )
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> notificationManager.subscribe(request))
                .withMessageContaining("Auth is required");
        }

        @Test
        void failsIfAuthContainsMoreThan256Characters() {
            var request = new CreateSubscriptionRequestDTO(
                "https://example.org/3",
                new SubscriptionKeysDTO(
                    "key3",
                    "a".repeat(257)
                )
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> notificationManager.subscribe(request))
                .withMessageContaining("Auth cannot contain more than 256 characters");
        }

        @Test
        void failsIfKeyIsMissing() {
            var request = new CreateSubscriptionRequestDTO(
                "https://example.org/3",
                new SubscriptionKeysDTO(
                    null,
                    "auth3"
                )
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> notificationManager.subscribe(request))
                .withMessageContaining("Key is required");
        }

        @Test
        void failsIfKeyContainsMoreThan256Characters() {
            var request = new CreateSubscriptionRequestDTO(
                "https://example.org/3",
                new SubscriptionKeysDTO(
                    "a".repeat(257),
                    "auth3"
                )
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> notificationManager.subscribe(request))
                .withMessageContaining("Key cannot contain more than 256 characters");
        }
    }

    @Nested
    class unsubscribe {
        @Test
        void deletesEntity() {
            var user = new UserDTO(
                "auth|3743a8f73e0d4c06baa4",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            notificationManager.unsubscribe();
            assertThat(subscriptionRepository.existsById(user.id())).isFalse();
        }

        @Test
        void doesNothingIfNoSubscriptionFound() {
            var user = new UserDTO(
                "auth|ff9d85fcc3c505949092c",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            notificationManager.unsubscribe();
            assertThat(subscriptionRepository.count()).isEqualTo(2);
        }

        @Test
        void failsIfUserNotAuthenticated() {
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidNotificationException.class)
                .isThrownBy(() -> notificationManager.unsubscribe())
                .withMessage("User is not authenticated");
        }
    }

    @Nested
    class findAll {
        @Test
        void returnsResults() {
            var user = new UserDTO(
                "auth|3743a8f73e0d4c06baa4",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenReturn(user);
            Page<NotificationDTO> results = notificationManager.findAll(pageRequest);
            assertThat(results).containsExactly(
                new NotificationDTO(
                    UUID.fromString("db1a169c-376d-4d77-8d3d-17962911d468"),
                    NotificationType.CABINET_ENTRY_EXPIRED,
                    "Title 1",
                    "Message 1"
                ),
                new NotificationDTO(
                    UUID.fromString("3dd0fd05-baee-4445-b9ec-432cf4b8f13d"),
                    NotificationType.CABINET_ENTRY_EXPIRED,
                    "Title 2",
                    "Message 2"
                )
            );
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidNotificationException.class)
                .isThrownBy(() -> notificationManager.findAll(pageRequest))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfPageRequestNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> notificationManager.findAll(null));
        }
    }

    @Nested
    class delete {
        @Test
        void setsDeleted() {
            UUID id = UUID.fromString("db1a169c-376d-4d77-8d3d-17962911d468");
            var user = new UserDTO(
                "auth|3743a8f73e0d4c06baa4",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            notificationManager.delete(id);
            NotificationEntity result = notificationRepository.findById(id).orElseThrow();
            assertThat(result.isActive()).isFalse();
        }

        @Test
        void failsIfNotificationNotFound() {
            UUID id = UUID.fromString("bf844a6a-7d75-44a6-ab06-67757304f124");
            var user = new UserDTO(
                "auth|3743a8f73e0d4c06baa4",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(NotificationNotFoundException.class)
                .isThrownBy(() -> notificationManager.delete(id))
                .withMessage("Notification with ID 'bf844a6a-7d75-44a6-ab06-67757304f124' does not exist");
        }

        @Test
        void failsIfUserNotAuthenticated() {
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidNotificationException.class)
                .isThrownBy(() -> notificationManager.delete(UUID.randomUUID()))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> notificationManager.delete(null));
        }
    }

    @Nested
    class findConfiguration {
        @Test
        void returnsPublicKey() {
            SubscriptionConfigurationDTO result = notificationManager.findConfiguration();
            assertThat(result).isEqualTo(new SubscriptionConfigurationDTO(
                "BIyq6YYFYOCttqL-N22xS84_EfO2CFYhn86ZW4gkzIK_uTht7rofUlIrXpu_r4-BT-qmf2TZFAq92jKhcBFIF-w"
            ));
        }
    }
}