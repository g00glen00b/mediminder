package codes.dimitri.mediminder.api.user.implementation.cleanup;

import codes.dimitri.mediminder.api.shared.TestClockConfiguration;
import codes.dimitri.mediminder.api.user.implementation.UserEntity;
import codes.dimitri.mediminder.api.user.implementation.UserEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ApplicationModuleTest(extraIncludes = "common")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.mail.host=dummy",
    "user.verification-url=http://example.org/user/verify?code=%s",
    "user.password-reset-url=http://example.org/user/confirm-password-reset?code=%s",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2",
    "spring.batch.job.enabled=false"
})
@Import({
    TestClockConfiguration.class
})
@Sql("classpath:test-data/users.sql")
@Sql(value = "classpath:test-data/cleanup-users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserCodeCleanupBatchTest {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    @Qualifier("userCodeCleanupJob")
    private Job job;
    @Autowired
    private UserEntityRepository repository;

    @Test
    void jobCleansUpUsers() throws Exception {
        var utils = getJobLauncherTestUtils(job);
        var jobExecution = utils.launchJob();
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(repository.findAll())
            .hasSize(5)
            .extracting(UserEntity::getId)
            .doesNotContain(UUID.fromString("0f1f19c2-2d09-43b9-a7fc-ce82b9bfe43a"));
    }

    @Test
    void jobCleansUpUnusedPasswordResetCodes() throws Exception {
        var utils = getJobLauncherTestUtils(job);
        var jobExecution = utils.launchJob();
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(repository.findAll())
            .extracting(UserEntity::getPasswordResetCode)
            .doesNotContain("code2")
            .contains("code1");
    }

    private JobLauncherTestUtils getJobLauncherTestUtils(Job job) {
        JobLauncherTestUtils jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJobRepository(jobRepository);
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
        jobLauncherTestUtils.setJob(job);
        return jobLauncherTestUtils;
    }
}