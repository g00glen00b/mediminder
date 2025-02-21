package codes.dimitri.mediminder.api.user.implementation.cleanup;

import codes.dimitri.mediminder.api.user.implementation.UserEntityRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBatchTest
@ApplicationModuleTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:///mediminder"
})
@Sql(value = "classpath:test-data/user-batch.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:test-data/cleanup-user-batch.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserCodeCleanupBatchTest {
    private static final ZonedDateTime TODAY = ZonedDateTime.of(2024, 6, 29, 11, 0, 0, 0, ZoneId.of("UTC"));
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;
    @Autowired
    private UserEntityRepository repository;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private JavaMailSender mailSender;

    @AfterEach
    void tearDown() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    void job() throws Exception {
        assertThat(repository.count()).isEqualTo(6);
        jobLauncherTestUtils.launchJob();
        assertThat(repository.count()).isEqualTo(5);
        assertThat(repository.existsByPasswordResetCode("code1")).isTrue();
        assertThat(repository.existsByPasswordResetCode("code2")).isFalse();
        assertThat(repository.existsByVerificationCode("code3")).isTrue();
        assertThat(repository.existsByVerificationCode("code4")).isFalse();
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        public Clock clock() {
            return Clock.fixed(TODAY.toInstant(), TODAY.getZone());
        }
    }
}