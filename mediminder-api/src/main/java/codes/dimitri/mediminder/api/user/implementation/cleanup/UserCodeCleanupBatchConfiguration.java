package codes.dimitri.mediminder.api.user.implementation.cleanup;

import codes.dimitri.mediminder.api.user.implementation.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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
class UserCodeCleanupBatchConfiguration {
    private final JobRepository jobRepository;
    private final UserCodeCleanupProperties properties;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job userCodeCleanupJob(
        @Qualifier("deleteUnverifiedUserStep") Step deleteUnverifiedUserStep,
        @Qualifier("unsetUnusedPasswordResetCodeStep") Step unsetUnusedPasswordResetCodeStep) {
        return new JobBuilder("userCodeCleanupJob", jobRepository)
            .start(deleteUnverifiedUserStep)
            .next(unsetUnusedPasswordResetCodeStep)
            .build();
    }

    @Bean
    public Step deleteUnverifiedUserStep(UnverifiedUserCleanupReader reader, UserEntityDeleteWriter writer) {
        return new StepBuilder("deleteUnverifiedUserStep", jobRepository)
            .<UserEntity, UserEntity>chunk(properties.chunkSize(), transactionManager)
            .reader(reader)
            .writer(writer)
            .build();
    }

    @Bean
    public Step unsetUnusedPasswordResetCodeStep(
        UnusedPasswordResetUserCleanupReader reader,
        UnusedPasswordResetUserCleanupProcessor processor,
        UserEntitySaveWriter writer) {
        return new StepBuilder("unsetUnusedPasswordResetCodeStep", jobRepository)
            .<UserEntity, UserEntity>chunk(properties.chunkSize(), transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }

    @Bean
    public ScheduledFuture<?> userCodeCleanupBatchScheduler(UserCodeCleanupBatchTask task, TaskScheduler scheduler) {
        CronTrigger trigger = new CronTrigger(properties.schedule());
        return scheduler.schedule(task, trigger);
    }
}
