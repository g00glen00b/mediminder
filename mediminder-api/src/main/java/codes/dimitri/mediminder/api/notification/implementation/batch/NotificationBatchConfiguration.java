package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryDTO;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationProperties;
import codes.dimitri.mediminder.api.schedule.ScheduleDTO;
import codes.dimitri.mediminder.api.schedule.UserScheduledMedicationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.ScheduledFuture;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
class NotificationBatchConfiguration {
    private final JobRepository jobRepository;
    private final NotificationProperties properties;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job notificationJob(
        @Qualifier("notificationCleanupStep") Step notificationCleanupStep,
        @Qualifier("outOfDoseStep") Step outOfDoseStep,
        @Qualifier("expiryStep") Step expiryStep,
        @Qualifier("intakeStep") Step intakeStep) {
        return new JobBuilder("notificationJob", jobRepository)
            .start(notificationCleanupStep)
            .next(outOfDoseStep)
            .next(expiryStep)
            .next(intakeStep)
            .build();
    }

    @Bean
    public Step notificationCleanupStep(NotificationCleanupTasklet tasklet) {
        return new StepBuilder("notificationCleanupStep", jobRepository)
            .tasklet(tasklet, transactionManager)
            .build();
    }

    @Bean
    public CompositeItemProcessor<UserScheduledMedicationDTO, NotificationEntity> compositeOutOfDoseProcessor(
        UserScheduledMedicationNotificationProcessor processor,
        NotificationExistenceProcessor existenceProcessor
    ) {
        return new CompositeItemProcessor<>(processor, existenceProcessor);
    }

    @Bean
    public CompositeItemProcessor<CabinetEntryDTO, NotificationEntity> compositeExpiryProcessor(
        CabinetEntryExpiryNotificationProcessor processor,
        NotificationExistenceProcessor existenceProcessor
    ) {
        return new CompositeItemProcessor<>(processor, existenceProcessor);
    }

    @Bean
    public CompositeItemProcessor<ScheduleDTO, NotificationEntity> compositeIntakeProcessor(
        ActiveScheduleNotificationProcessor processor,
        NotificationExistenceProcessor existenceProcessor
    ) {
        return new CompositeItemProcessor<>(processor, existenceProcessor);
    }

    @Bean
    public CompositeItemWriter<NotificationEntity> compositeNotificationWriter(
        NotificationItemWriter persistenceWriter,
        PushNotificationWriter pushNotificationWriter
    ) {
        return new CompositeItemWriter<>(persistenceWriter, pushNotificationWriter);
    }

    @Bean
    public Step outOfDoseStep(
        UserScheduledMedicationReader reader,
        @Qualifier("compositeOutOfDoseProcessor") CompositeItemProcessor<UserScheduledMedicationDTO, NotificationEntity> compositeOutOfDoseProcessor,
        @Qualifier("compositeNotificationWriter") CompositeItemWriter<NotificationEntity> writer
    ) {
        return new StepBuilder("outOfDoseStep", jobRepository)
            .<UserScheduledMedicationDTO, NotificationEntity>chunk(properties.chunkSize(), transactionManager)
            .reader(reader)
            .processor(compositeOutOfDoseProcessor)
            .writer(writer)
            .build();
    }

    @Bean
    public Step expiryStep(
        CabinetEntryWithNearExpiryDateReader reader,
        @Qualifier("compositeExpiryProcessor") CompositeItemProcessor<CabinetEntryDTO, NotificationEntity> compositeExpiryProcessor,
        @Qualifier("compositeNotificationWriter") CompositeItemWriter<NotificationEntity> writer
    ) {
        return new StepBuilder("expiryStep", jobRepository)
            .<CabinetEntryDTO, NotificationEntity>chunk(properties.chunkSize(), transactionManager)
            .reader(reader)
            .processor(compositeExpiryProcessor)
            .writer(writer)
            .build();
    }

    @Bean
    public Step intakeStep(
        ActiveScheduleReader reader,
        @Qualifier("compositeIntakeProcessor") CompositeItemProcessor<ScheduleDTO, NotificationEntity> compositeIntakeProcessor,
        @Qualifier("compositeNotificationWriter") CompositeItemWriter<NotificationEntity> writer
    ) {
        return new StepBuilder("intakeStep", jobRepository)
            .<ScheduleDTO, NotificationEntity>chunk(properties.chunkSize(), transactionManager)
            .reader(reader)
            .processor(compositeIntakeProcessor)
            .writer(writer)
            .build();
    }

    @Bean
    public ScheduledFuture<?> notificationBatchScheduler(NotificationBatchTask task, TaskScheduler scheduler) {
        CronTrigger trigger = new CronTrigger(properties.schedule());
        return scheduler.schedule(task, trigger);
    }
}
