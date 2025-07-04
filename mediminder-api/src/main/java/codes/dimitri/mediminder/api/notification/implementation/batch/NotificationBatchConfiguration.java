package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryDTO;
import codes.dimitri.mediminder.api.document.DocumentDTO;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationProperties;
import codes.dimitri.mediminder.api.schedule.UserScheduledMedicationDTO;
import codes.dimitri.mediminder.api.user.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
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
        @Qualifier("documentExpiryStep") Step documentExpiryStep,
        @Qualifier("intakeStep") Step intakeStep) {
        return new JobBuilder("notificationJob", jobRepository)
            .start(notificationCleanupStep)
            .next(outOfDoseStep)
            .next(expiryStep)
            .next(documentExpiryStep)
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
    public CompositeItemProcessor<DocumentDTO, NotificationEntity> compositeDocumentExpiryProcessor(
        DocumentExpiryNotificationProcessor processor,
        NotificationExistenceProcessor existenceProcessor
    ) {
        return new CompositeItemProcessor<>(processor, existenceProcessor);
    }

    @Bean
    public CompositeItemProcessor<UserDTO, List<NotificationEntity>> compositeIntakeProcessor(
        UserIncompleteIntakeEventProcessor processor,
        NotificationExistenceProcessor existenceProcessor
    ) {
        return new CompositeItemProcessor<>(processor, new FlatteningItemProcessor<>(existenceProcessor));
    }

    @Bean
    public CompositeItemWriter<NotificationEntity> compositeNotificationWriter(
        NotificationItemWriter persistenceWriter,
        PushNotificationWriter pushNotificationWriter
    ) {
        return new CompositeItemWriter<>(persistenceWriter, pushNotificationWriter);
    }

    @Bean
    public ItemWriter<List<NotificationEntity>> flatteningCompositeNotificationWriter(
        @Qualifier("compositeNotificationWriter") CompositeItemWriter<NotificationEntity> writer
    ) {
        return new FlatteningItemWriter<>(writer);
    }

    @Bean
    public Step outOfDoseStep(
        UserScheduledMedicationReader reader,
        @Qualifier("compositeOutOfDoseProcessor") CompositeItemProcessor<UserScheduledMedicationDTO, NotificationEntity> compositeOutOfDoseProcessor,
        CabinetEntryExpiryNotificationProcessor cabinetEntryExpiryNotificationProcessor,
        @Qualifier("compositeNotificationWriter") CompositeItemWriter<NotificationEntity> writer
    ) {
        return new StepBuilder("outOfDoseStep", jobRepository)
            .<UserScheduledMedicationDTO, NotificationEntity>chunk(properties.chunkSize(), transactionManager)
            .reader(reader)
            .processor(compositeOutOfDoseProcessor)
            .listener(cabinetEntryExpiryNotificationProcessor)
            .writer(writer)
            .build();
    }

    @Bean
    public Step expiryStep(
        CabinetEntryWithNearExpiryDateReader reader,
        CabinetEntryExpiryNotificationProcessor cabinetEntryExpiryNotificationProcessor,
        @Qualifier("compositeExpiryProcessor") CompositeItemProcessor<CabinetEntryDTO, NotificationEntity> compositeExpiryProcessor,
        @Qualifier("compositeNotificationWriter") CompositeItemWriter<NotificationEntity> writer
    ) {
        return new StepBuilder("expiryStep", jobRepository)
            .<CabinetEntryDTO, NotificationEntity>chunk(properties.chunkSize(), transactionManager)
            .reader(reader)
            .processor(compositeExpiryProcessor)
            .writer(writer)
            .listener(cabinetEntryExpiryNotificationProcessor)
            .build();
    }

    @Bean
    public Step documentExpiryStep(
        DocumentWithNearExpiryDateReader reader,
        DocumentExpiryNotificationProcessor documentExpiryNotificationProcessor,
        @Qualifier("compositeDocumentExpiryProcessor") CompositeItemProcessor<DocumentDTO, NotificationEntity> compositeDocumentExpiryProcessor,
        @Qualifier("compositeNotificationWriter") CompositeItemWriter<NotificationEntity> writer
    ) {
        return new StepBuilder("documentExpiryStep", jobRepository)
            .<DocumentDTO, NotificationEntity>chunk(properties.chunkSize(), transactionManager)
            .reader(reader)
            .processor(compositeDocumentExpiryProcessor)
            .writer(writer)
            .listener(documentExpiryNotificationProcessor)
            .build();
    }

    @Bean
    public Step intakeStep(
        UserReader reader,
        @Qualifier("compositeIntakeProcessor") CompositeItemProcessor<UserDTO, List<NotificationEntity>> compositeIntakeProcessor,
        @Qualifier("flatteningCompositeNotificationWriter") ItemWriter<List<NotificationEntity>> writer
    ) {
        return new StepBuilder("intakeStep", jobRepository)
            .<UserDTO, List<NotificationEntity>>chunk(properties.chunkSize(), transactionManager)
            .reader(reader)
            .processor(compositeIntakeProcessor)
            .writer(writer)
            .build();
    }
}
