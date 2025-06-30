package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.notification.NotificationType;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntityRepository;
import codes.dimitri.mediminder.api.notification.implementation.SubscriptionEntityRepository;
import codes.dimitri.mediminder.api.shared.TestClockConfiguration;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JsonContentAssert;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBatchTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2",
    "notification.public-key=BIyq6YYFYOCttqL-N22xS84_EfO2CFYhn86ZW4gkzIK_uTht7rofUlIrXpu_r4-BT-qmf2TZFAq92jKhcBFIF-w",
    "notification.private-key=CX5aOzJFXYQszpj__Trqa9GOIupZMLRrubTxsc3zNg0",
    "spring.batch.job.enabled=false",
    "spring.ai.model.chat=openai",
    "spring.ai.openai.api-key=dummy"
})
@Import({
    TestClockConfiguration.class
})
@Sql("classpath:test-data/notification-batch.sql")
@Sql(value = {
    "classpath:test-data/cleanup-notification.sql",
    "classpath:test-data/cleanup-cabinet-entries.sql",
    "classpath:test-data/cleanup-schedules.sql",
    "classpath:test-data/cleanup-users.sql",
    "classpath:test-data/cleanup-medication.sql",
    "classpath:test-data/cleanup-subscriptions.sql",
    "classpath:test-data/cleanup-documents.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class NotificationBatchTest {
    private static final String USER_ID = "auth|ff9d85fcc3c505949092c";
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    @Qualifier("notificationJob")
    private Job job;
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;
    @Autowired
    private NotificationEntityRepository repository;
    @Autowired
    private SubscriptionEntityRepository subscriptionRepository;
    @MockitoSpyBean
    private PushService pushService;
    @Captor
    private ArgumentCaptor<Notification> anyNotificiation;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(job);
    }

    @AfterEach
    void tearDown() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void createsNotifications() throws Exception {
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        var jobExecution = jobLauncherTestUtils.launchJob(parameters);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(repository.count()).isEqualTo(7);
    }

    @Test
    void doesNotSendAPushNotificationIfNoSubscriptionRegistered() throws Exception {
        subscriptionRepository.deleteAll();
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        verifyNoInteractions(pushService);
    }

    @Test
    void createsOutOfDoseNotification() throws Exception {
        UUID medicationId = UUID.fromString("cd7637ae-fda8-413a-a5e4-c0e1f0f68325");
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        assertThat(repository.findAll())
            .filteredOn(entity -> medicationId.equals(entity.getInitiatorId()))
            .singleElement()
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new NotificationEntity(
                USER_ID,
                NotificationType.SCHEDULE_OUT_OF_DOSES,
                medicationId,
                "Out of medication",
                "You ran out of Hydrocortisone 14mg",
                ZonedDateTime.of(2025, 3, 5, 10, 0, 0, 0, ZoneOffset.UTC).toInstant()
            ));
    }

    @Test
    void sendsOutOfDosePushNotification() throws Exception {
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        verify(pushService, atLeastOnce()).send(anyNotificiation.capture());
        assertThat(anyNotificiation.getAllValues())
            .extracting(Notification::getPayload)
            .map(String::new)
            .map(payload -> new JsonContentAssert(PushNotificationPayloadWrapper.class, payload))
            .anySatisfy(payload -> {
                payload.extractingJsonPathStringValue("$.notification.title").isEqualTo("Out of medication");
                payload.extractingJsonPathStringValue("$.notification.body").isEqualTo("You ran out of Hydrocortisone 14mg");
            });
    }

    @Test
    void createsAlmostOutOfDoseNotification() throws Exception {
        UUID medicationId = UUID.fromString("c266f875-0033-4cad-b96f-e17c37c81b66");
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        assertThat(repository.findAll())
            .filteredOn(entity -> medicationId.equals(entity.getInitiatorId()))
            .singleElement()
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new NotificationEntity(
                USER_ID,
                NotificationType.SCHEDULE_ALMOST_OUT_OF_DOSES,
                medicationId,
                "Almost out of medication",
                "You will soon run out of Hydrocortisone 8mg",
                ZonedDateTime.of(2025, 3, 5, 10, 0, 0, 0, ZoneOffset.UTC).toInstant()
            ));
    }

    @Test
    void sendsAlmostOutOfDosePushNotification() throws Exception {
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        verify(pushService, atLeastOnce()).send(anyNotificiation.capture());
        assertThat(anyNotificiation.getAllValues())
            .extracting(Notification::getPayload)
            .map(String::new)
            .map(payload -> new JsonContentAssert(PushNotificationPayloadWrapper.class, payload))
            .anySatisfy(payload -> {
                payload.extractingJsonPathStringValue("$.notification.title").isEqualTo("Almost out of medication");
                payload.extractingJsonPathStringValue("$.notification.body").isEqualTo("You will soon run out of Hydrocortisone 8mg");
            });
    }

    @Test
    void createsExpiredNotification() throws Exception {
        UUID cabinetEntryId = UUID.fromString("abbde92d-3746-4ae7-abab-be9e8a6c80de");
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        assertThat(repository.findAll())
            .filteredOn(entity -> cabinetEntryId.equals(entity.getInitiatorId()))
            .singleElement()
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new NotificationEntity(
                USER_ID,
                NotificationType.CABINET_ENTRY_EXPIRED,
                cabinetEntryId,
                "Cabinet entry expired",
                "A cabinet entry for 'Dafalgan' is expired",
                ZonedDateTime.of(2025, 3, 5, 10, 0, 0, 0, ZoneOffset.UTC).toInstant()
            ));
    }

    @Test
    void sendsExpiredPushNotification() throws Exception {
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        verify(pushService, atLeastOnce()).send(anyNotificiation.capture());
        assertThat(anyNotificiation.getAllValues())
            .extracting(Notification::getPayload)
            .map(String::new)
            .map(payload -> new JsonContentAssert(PushNotificationPayloadWrapper.class, payload))
            .anySatisfy(payload -> {
                payload.extractingJsonPathStringValue("$.notification.title").isEqualTo("Cabinet entry expired");
                payload.extractingJsonPathStringValue("$.notification.body").isEqualTo("A cabinet entry for 'Dafalgan' is expired");
            });
    }

    @Test
    void createsAlmostExpiredNotification() throws Exception {
        UUID cabinetEntryId = UUID.fromString("922b8f73-cd63-4e59-ae89-6d8415e6ef0e");
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        assertThat(repository.findAll())
            .filteredOn(entity -> cabinetEntryId.equals(entity.getInitiatorId()))
            .singleElement()
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new NotificationEntity(
                USER_ID,
                NotificationType.CABINET_ENTRY_ALMOST_EXPIRED,
                cabinetEntryId,
                "Cabinet entry almost expired",
                "A cabinet entry for 'Dafalgan' will expire on February 27, 2025",
                ZonedDateTime.of(2025, 3, 5, 10, 0, 0, 0, ZoneOffset.UTC).toInstant()
            ));
    }

    @Test
    void sendsAlmostExpiredPushNotification() throws Exception {
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        verify(pushService, atLeastOnce()).send(anyNotificiation.capture());
        assertThat(anyNotificiation.getAllValues())
            .extracting(Notification::getPayload)
            .map(String::new)
            .map(payload -> new JsonContentAssert(PushNotificationPayloadWrapper.class, payload))
            .anySatisfy(payload -> {
                payload.extractingJsonPathStringValue("$.notification.title").isEqualTo("Cabinet entry almost expired");
                payload.extractingJsonPathStringValue("$.notification.body").isEqualTo("A cabinet entry for 'Dafalgan' will expire on February 27, 2025");
            });
    }

    @Test
    void createsExpiredDocumentNotification() throws Exception {
        UUID documentId = UUID.fromString("d1ccc34f-7fc3-4f65-b4da-8ae8ff0accf0");
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        assertThat(repository.findAll())
            .filteredOn(entity -> documentId.equals(entity.getInitiatorId()))
            .singleElement()
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new NotificationEntity(
                USER_ID,
                NotificationType.DOCUMENT_EXPIRED,
                documentId,
                "Document expired",
                "Document 'file1.pdf' is expired",
                ZonedDateTime.of(2025, 3, 5, 10, 0, 0, 0, ZoneOffset.UTC).toInstant()
            ));
    }

    @Test
    void sendsExpiredDocumentPushNotification() throws Exception {
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        verify(pushService, atLeastOnce()).send(anyNotificiation.capture());
        assertThat(anyNotificiation.getAllValues())
            .extracting(Notification::getPayload)
            .map(String::new)
            .map(payload -> new JsonContentAssert(PushNotificationPayloadWrapper.class, payload))
            .anySatisfy(payload -> {
                payload.extractingJsonPathStringValue("$.notification.title").isEqualTo("Document expired");
                payload.extractingJsonPathStringValue("$.notification.body").isEqualTo("Document 'file1.pdf' is expired");
            });
    }

    @Test
    void createsAlmostExpiredDocumentNotification() throws Exception {
        UUID documentId = UUID.fromString("691090bd-1142-4265-9fde-c2e744a282c1");
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        assertThat(repository.findAll())
            .filteredOn(entity -> documentId.equals(entity.getInitiatorId()))
            .singleElement()
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new NotificationEntity(
                USER_ID,
                NotificationType.DOCUMENT_ALMOST_EXPIRED,
                documentId,
                "Document almost expired",
                "Document 'file2.pdf' will expire on February 27, 2025",
                ZonedDateTime.of(2025, 3, 5, 10, 0, 0, 0, ZoneOffset.UTC).toInstant()
            ));
    }

    @Test
    void sendsAlmostExpiredDocumentPushNotification() throws Exception {
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        verify(pushService, atLeastOnce()).send(anyNotificiation.capture());
        assertThat(anyNotificiation.getAllValues())
            .extracting(Notification::getPayload)
            .map(String::new)
            .map(payload -> new JsonContentAssert(PushNotificationPayloadWrapper.class, payload))
            .anySatisfy(payload -> {
                payload.extractingJsonPathStringValue("$.notification.title").isEqualTo("Document almost expired");
                payload.extractingJsonPathStringValue("$.notification.body").isEqualTo("Document 'file2.pdf' will expire on February 27, 2025");
            });
    }

    @Test
    void createsIntakeNotification() throws Exception {
        UUID scheduleId = UUID.fromString("61b1056e-66b2-4665-9d65-3469cb7b8ffe");
        var date = LocalDateTime.of(2025, 2, 26, 10, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        assertThat(repository.findAll())
            .filteredOn(entity -> scheduleId.equals(entity.getInitiatorId()))
            .singleElement()
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new NotificationEntity(
                USER_ID,
                NotificationType.INTAKE_EVENT,
                scheduleId,
                "Time to take your medicine",
                "You have to take 'Dafalgan' at 10:00",
                ZonedDateTime.of(2025, 2,26, 12, 0, 0, 0, ZoneOffset.UTC).toInstant()
            ));
    }

    @Test
    void sendsIntakePushNotification() throws Exception {
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        jobLauncherTestUtils.launchJob(parameters);
        verify(pushService, atLeastOnce()).send(anyNotificiation.capture());
        assertThat(anyNotificiation.getAllValues())
            .extracting(Notification::getPayload)
            .map(String::new)
            .map(payload -> new JsonContentAssert(PushNotificationPayloadWrapper.class, payload))
            .anySatisfy(payload -> {
                payload.extractingJsonPathStringValue("$.notification.title").isEqualTo("Time to take your medicine");
                payload.extractingJsonPathStringValue("$.notification.body").isEqualTo("You have to take 'Dafalgan' at 10:00");
            });
    }

    @Test
    void cleansUpOldNotifications() throws Exception {
        var id = UUID.fromString("2272b371-d6df-420e-b875-875321deb7e7");
        var date = LocalDateTime.of(2025, 2, 26, 0, 0);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", date)
            .toJobParameters();
        assertThat(repository.existsById(id)).isTrue();
        jobLauncherTestUtils.launchJob(parameters);
        assertThat(repository.existsById(id)).isFalse();
    }
}