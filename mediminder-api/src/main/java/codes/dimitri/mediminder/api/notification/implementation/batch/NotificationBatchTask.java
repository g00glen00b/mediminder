package codes.dimitri.mediminder.api.notification.implementation.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Component
public class NotificationBatchTask implements Runnable {
    private final Clock clock;
    private final Job notificationJob;
    private final JobLauncher jobLauncher;

    public NotificationBatchTask(Clock clock, @Qualifier("notificationJob") Job notificationJob, JobLauncher jobLauncher) {
        this.clock = clock;
        this.notificationJob = notificationJob;
        this.jobLauncher = jobLauncher;
    }

    @Override
    public void run() {
        JobParameters parameters = new JobParametersBuilder()
            .addLocalDateTime("date", LocalDateTime.now(clock))
            .toJobParameters();
        try {
            jobLauncher.run(notificationJob, parameters);
        } catch (JobExecutionAlreadyRunningException | JobParametersInvalidException |
                 JobInstanceAlreadyCompleteException | JobRestartException ex) {
            log.error("Could not run job", ex);
        }
    }
}
