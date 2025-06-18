package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.user.UserDeletedEvent;
import codes.dimitri.mediminder.api.user.UserManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2"
})
@Transactional
@Sql("classpath:test-data/medication.sql")
@Sql(value = "classpath:test-data/cleanup-medication.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MedicationEventListenerTest {
    @Autowired
    private MedicationEventListener listener;
    @Autowired
    private MedicationEntityRepository repository;
    @MockitoBean
    private UserManager userManager;

    @Nested
    class handleUserDeletedEvent {
        @Test
        void deletesAllMedicationsForUser() {
            assertThat(repository.count()).isEqualTo(6);
            String userId = "auth|2e4aadf46d7e4bd1ad9f";
            listener.handleUserDeletedEvent(new UserDeletedEvent(userId));
            assertThat(repository.count()).isEqualTo(3);
        }
    }
}