package codes.dimitri.mediminder.api.user.implementation.cleanup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class UserCodeCleanupBatchTaskTest {
    private static final ZonedDateTime TODAY = ZonedDateTime.of(2024, 6, 29, 11, 0, 0, 0, ZoneId.of("UTC"));
    private UserCodeCleanupBatchTask task;
    @Mock
    private Job job;
    @Mock
    private JobLauncher jobLauncher;
    @Captor
    private ArgumentCaptor<JobParameters> anyJobParameters;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.toInstant(), TODAY.getZone());
        task = new UserCodeCleanupBatchTask(clock, job, jobLauncher);
    }

    @Nested
    class run {
        @Test
        void launchesJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
            task.run();
            verify(jobLauncher).run(eq(job), anyJobParameters.capture());
            assertThat(anyJobParameters.getValue().getLocalDateTime("date")).isEqualTo(TODAY.toLocalDateTime());
        }

        @Test
        void logsAlreadyComplete(CapturedOutput output) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
            doThrow(new JobInstanceAlreadyCompleteException("Already complete")).when(jobLauncher).run(any(), any());
            task.run();
            assertThat(output.getOut()).contains("Could not run job");
            assertThat(output.getOut()).contains("Already complete");
        }

        @Test
        void logsAlreadyRunning(CapturedOutput output) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
            doThrow(new JobExecutionAlreadyRunningException("Already running")).when(jobLauncher).run(any(), any());
            task.run();
            assertThat(output.getOut()).contains("Could not run job");
            assertThat(output.getOut()).contains("Already running");
        }

        @Test
        void logsInvalidParameters(CapturedOutput output) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
            doThrow(new JobParametersInvalidException("Invalid parameters")).when(jobLauncher).run(any(), any());
            task.run();
            assertThat(output.getOut()).contains("Could not run job");
            assertThat(output.getOut()).contains("Invalid parameters");
        }

        @Test
        void logsJobRestart(CapturedOutput output) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
            doThrow(new JobRestartException("Illegal restart")).when(jobLauncher).run(any(), any());
            task.run();
            assertThat(output.getOut()).contains("Could not run job");
            assertThat(output.getOut()).contains("Illegal restart");
        }
    }
}