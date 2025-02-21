package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryDTO;
import codes.dimitri.mediminder.api.cabinet.CabinetEntryManager;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.notification.NotificationType;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntityRepository;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.schedule.SchedulePeriodDTO;
import codes.dimitri.mediminder.api.schedule.UserScheduledMedicationDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBatchTest
@ApplicationModuleTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.profiles.active=integration",
    "spring.datasource.url=jdbc:tc:postgresql:///mediminder",
    "notification.public-key=BIwOqnjx8SkLIp6LpFm6AaeREBVsWvHncjCROWRK00MWZ8kudiq20hdYLB8Ep_a5ft2yvqJDSyvsCmiowYp6PrU",
    "notification.private-key=NY7fftrcny36ogBFeSmGFqVrVMFS3GrBiy4x1qqghfE"
})
@Sql(value = "classpath:test-data/notification-batch.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:test-data/cleanup-notification-batch.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class NotificationBatchTest {
    private static final ZonedDateTime TODAY = ZonedDateTime.of(2024, 6, 3, 9, 0, 0, 0, ZoneId.of("UTC"));
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;
    @Autowired
    private NotificationEntityRepository repository;
    @MockBean
    private ScheduleManager scheduleManager;
    @MockBean
    private MedicationManager medicationManager;
    @MockBean
    private CabinetEntryManager cabinetEntryManager;
    @MockBean
    private UserManager userManager;

    @AfterEach
    void tearDown() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void clear_cleansOutdatedNotifications() throws Exception {
        // Given
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        // Then
        assertThat(repository.count()).isEqualTo(3);
        jobLauncherTestUtils.launchJob(parameters);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void outOfDose_createsNotification() throws Exception {
        // Given
        var medication = Instancio.create(MedicationDTO.class);
        var userScheduledMedication = Instancio.of(UserScheduledMedicationDTO.class)
            .set(field(UserScheduledMedicationDTO::medicationId), medication.id())
            .create();
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        var pageRequest = PageRequest.of(0, 1);
        // When
        when(scheduleManager.findAllUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of(userScheduledMedication)));
        when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(any())).thenReturn(BigDecimal.ZERO);
        when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
        // Then
        jobLauncherTestUtils.launchJob(parameters);
        Page<NotificationEntity> result = repository.findAllActiveByUserId(userScheduledMedication.userId(), pageRequest);
        assertThat(result)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
            .containsOnly(new NotificationEntity(
               userScheduledMedication.userId(),
               NotificationType.SCHEDULE_OUT_OF_DOSES,
               userScheduledMedication.medicationId(),
               "Out of medication",
               "You ran out of " + medication.name(),
               TODAY.toInstant().plus(Period.ofWeeks(1))
            ));
    }

    @Test
    void outOfDose_retrievesData() throws Exception {
        // Given
        var medication = Instancio.create(MedicationDTO.class);
        var userScheduledMedication = Instancio.of(UserScheduledMedicationDTO.class)
            .set(field(UserScheduledMedicationDTO::medicationId), medication.id())
            .create();
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        // When
        when(scheduleManager.findAllUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of(userScheduledMedication)));
        when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(any())).thenReturn(BigDecimal.ZERO);
        when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
        // Then
        jobLauncherTestUtils.launchJob(parameters);
        verify(scheduleManager).findAllUserScheduledMedicationOnDate(TODAY.toLocalDate(), PageRequest.of(0, 10));
        verify(cabinetEntryManager).calculateTotalRemainingDosesByMedicationId(userScheduledMedication.medicationId());
        verify(medicationManager).findByIdAndUserId(medication.id(), userScheduledMedication.userId());
    }

    @Test
    void outOfDose_doesNothingIfMedicationNotFound() throws Exception {
        // Given
        var userScheduledMedication = Instancio.create(UserScheduledMedicationDTO.class);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        var pageRequest = PageRequest.of(0, 1);
        // When
        when(scheduleManager.findAllUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of(userScheduledMedication)));
        when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(any())).thenReturn(BigDecimal.ZERO);
        // Then
        jobLauncherTestUtils.launchJob(parameters);
        Page<NotificationEntity> result = repository.findAllActiveByUserId(userScheduledMedication.userId(), pageRequest);
        assertThat(result).isEmpty();
    }

    @Test
    void outOfDose_doesNotCreateNotificationIfAlreadyExisting() throws Exception {
        // Given
        var medication = Instancio.create(MedicationDTO.class);
        var userScheduledMedication = Instancio.of(UserScheduledMedicationDTO.class)
            .set(field(UserScheduledMedicationDTO::medicationId), medication.id())
            .create();
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        var pageRequest = PageRequest.of(0, 1);
        // When
        repository.save(new NotificationEntity(
            UUID.randomUUID(),
            userScheduledMedication.userId(),
            NotificationType.SCHEDULE_OUT_OF_DOSES,
            userScheduledMedication.medicationId(),
            "Test",
            "Test",
            TODAY.plusDays(1).toInstant(),
            false
        ));
        when(scheduleManager.findAllUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of(userScheduledMedication)));
        when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(any())).thenReturn(BigDecimal.ZERO);
        when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
        // Then
        jobLauncherTestUtils.launchJob(parameters);
        Page<NotificationEntity> result = repository.findAllActiveByUserId(userScheduledMedication.userId(), pageRequest);
        assertThat(result).isEmpty();
    }

    @Test
    void almostOutOfDose_createsNotification() throws Exception {
        // Given
        var medication = Instancio.create(MedicationDTO.class);
        var userScheduledMedication = Instancio.of(UserScheduledMedicationDTO.class)
            .set(field(UserScheduledMedicationDTO::medicationId), medication.id())
            .create();
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        var pageRequest = PageRequest.of(0, 1);
        // When
        when(scheduleManager.findAllUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of(userScheduledMedication)));
        when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(any())).thenReturn(new BigDecimal("10"));
        when(scheduleManager.calculateRequiredDoses(any(), any())).thenReturn(new BigDecimal("20"));
        when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
        when(userManager.calculateTodayForUser(any())).thenReturn(TODAY.toLocalDateTime());
        // Then
        jobLauncherTestUtils.launchJob(parameters);
        Page<NotificationEntity> result = repository.findAllActiveByUserId(userScheduledMedication.userId(), pageRequest);
        assertThat(result)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
            .containsOnly(new NotificationEntity(
                userScheduledMedication.userId(),
                NotificationType.SCHEDULE_ALMOST_OUT_OF_DOSES,
                userScheduledMedication.medicationId(),
                "Almost out of medication",
                "You will soon run out of " + medication.name(),
                TODAY.toInstant().plus(Period.ofWeeks(1))
            ));
    }

    @Test
    void almostOutOfDose_doesNothingIfMedicationNotFound() throws Exception {
        // Given
        var userScheduledMedication = Instancio.create(UserScheduledMedicationDTO.class);
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        var pageRequest = PageRequest.of(0, 1);
        // When
        when(scheduleManager.findAllUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of(userScheduledMedication)));
        when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(any())).thenReturn(new BigDecimal("10"));
        when(scheduleManager.calculateRequiredDoses(any(), any())).thenReturn(new BigDecimal("20"));
        when(userManager.calculateTodayForUser(any())).thenReturn(TODAY.toLocalDateTime());
        // Then
        jobLauncherTestUtils.launchJob(parameters);
        Page<NotificationEntity> result = repository.findAllActiveByUserId(userScheduledMedication.userId(), pageRequest);
        assertThat(result).isEmpty();
    }

    @Test
    void almostOutOfDose_retrievesData() throws Exception {
        // Given
        var medication = Instancio.create(MedicationDTO.class);
        var userScheduledMedication = Instancio.of(UserScheduledMedicationDTO.class)
            .set(field(UserScheduledMedicationDTO::medicationId), medication.id())
            .create();
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        // When
        when(scheduleManager.findAllUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of(userScheduledMedication)));
        when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(any())).thenReturn(new BigDecimal("10"));
        when(scheduleManager.calculateRequiredDoses(any(), any())).thenReturn(new BigDecimal("20"));
        when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
        when(userManager.calculateTodayForUser(any())).thenReturn(TODAY.toLocalDateTime());
        // Then
        jobLauncherTestUtils.launchJob(parameters);
        verify(scheduleManager).findAllUserScheduledMedicationOnDate(TODAY.toLocalDate(), PageRequest.of(0, 10));
        verify(cabinetEntryManager).calculateTotalRemainingDosesByMedicationId(userScheduledMedication.medicationId());
        verify(medicationManager).findByIdAndUserId(medication.id(), userScheduledMedication.userId());
        verify(scheduleManager).calculateRequiredDoses(medication.id(), new SchedulePeriodDTO(TODAY.toLocalDate(), TODAY.toLocalDate().plusWeeks(1)));
        verify(userManager).calculateTodayForUser(userScheduledMedication.userId());
    }

    @Test
    void almostOutOfDose_doesNotCreateNotificationIfAlreadyExisting() throws Exception {
        // Given
        var medication = Instancio.create(MedicationDTO.class);
        var userScheduledMedication = Instancio.of(UserScheduledMedicationDTO.class)
            .set(field(UserScheduledMedicationDTO::medicationId), medication.id())
            .create();
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        var pageRequest = PageRequest.of(0, 1);
        // When
        repository.save(new NotificationEntity(
            UUID.randomUUID(),
            userScheduledMedication.userId(),
            NotificationType.SCHEDULE_ALMOST_OUT_OF_DOSES,
            userScheduledMedication.medicationId(),
            "Test",
            "Test",
            TODAY.plusDays(1).toInstant(),
            false
        ));
        when(scheduleManager.findAllUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of(userScheduledMedication)));
        when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(any())).thenReturn(new BigDecimal("10"));
        when(scheduleManager.calculateRequiredDoses(any(), any())).thenReturn(new BigDecimal("20"));
        when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
        when(userManager.calculateTodayForUser(any())).thenReturn(TODAY.toLocalDateTime());
        // Then
        jobLauncherTestUtils.launchJob(parameters);
        Page<NotificationEntity> result = repository.findAllActiveByUserId(userScheduledMedication.userId(), pageRequest);
        assertThat(result).isEmpty();
    }

    @Test
    void almostOutOfDose_doesNothingIfNotAlmostOutOfDose() throws Exception {
        // Given
        var medication = Instancio.create(MedicationDTO.class);
        var userScheduledMedication = Instancio.of(UserScheduledMedicationDTO.class)
            .set(field(UserScheduledMedicationDTO::medicationId), medication.id())
            .create();
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        var pageRequest = PageRequest.of(0, 1);
        // When
        when(scheduleManager.findAllUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of(userScheduledMedication)));
        when(cabinetEntryManager.calculateTotalRemainingDosesByMedicationId(any())).thenReturn(new BigDecimal("10"));
        when(scheduleManager.calculateRequiredDoses(any(), any())).thenReturn(new BigDecimal("5"));
        when(medicationManager.findByIdAndUserId(any(), any())).thenReturn(Optional.of(medication));
        when(userManager.calculateTodayForUser(any())).thenReturn(TODAY.toLocalDateTime());
        // Then
        jobLauncherTestUtils.launchJob(parameters);
        Page<NotificationEntity> result = repository.findAllActiveByUserId(userScheduledMedication.userId(), pageRequest);
        assertThat(result).isEmpty();
    }

    @Test
    void expired_createsNotification() throws Exception {
        // Given
        var cabinetEntry = Instancio.of(CabinetEntryDTO.class)
            .set(field(CabinetEntryDTO::expiryDate), TODAY.toLocalDate())
            .create();
        var parameters = new JobParametersBuilder()
            .addLocalDateTime("date", TODAY.toLocalDateTime())
            .toJobParameters();
        var pageRequest = PageRequest.of(0, 1);
        // When
        when(scheduleManager.findAllUserScheduledMedicationOnDate(any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(cabinetEntryManager.findAllNonEmptyWithExpiryDateBefore(any(), any())).thenReturn(new PageImpl<>(List.of(cabinetEntry)));
        // Then
        jobLauncherTestUtils.launchJob(parameters);
        Page<NotificationEntity> result = repository.findAllActiveByUserId(cabinetEntry.userId(), pageRequest);
        assertThat(result)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
            .containsOnly(new NotificationEntity(
                cabinetEntry.userId(),
                NotificationType.CABINET_ENTRY_EXPIRED,
                cabinetEntry.id(),
                "Cabinet entry expired",
                "A cabinet entry for '" + cabinetEntry.medication().name() + "' is expired",
                TODAY.toInstant().plus(Period.ofWeeks(1))
            ));
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        public Clock clock() {
            return Clock.fixed(TODAY.toInstant(), TODAY.getZone());
        }
    }
}