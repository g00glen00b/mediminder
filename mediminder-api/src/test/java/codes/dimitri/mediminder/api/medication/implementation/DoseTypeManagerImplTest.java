package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.DoseTypeDTO;
import codes.dimitri.mediminder.api.medication.DoseTypeManager;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2"
})
@Transactional
@Sql("classpath:test-data/medication.sql")
@Sql(value = "classpath:test-data/cleanup-medication.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DoseTypeManagerImplTest {
    @Autowired
    private DoseTypeManager manager;

    @MockitoBean
    private UserManager userManager;

    @Nested
    class findAllByMedicationTypeId {
        @Test
        void returnsResults() {
            var pageRequest = PageRequest.of(0, 10);
            var results = manager.findAllByMedicationTypeId("SYRUP", pageRequest);
            assertThat(results).containsExactly(
                new DoseTypeDTO("DOSE", "dose(s)"),
                new DoseTypeDTO("MILLILITER", "milliliter")
            );
        }

        @Test
        void failsIfMedicationTypeIdNotGiven() {
            var pageRequest = PageRequest.of(0, 10);
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllByMedicationTypeId(null, pageRequest));
        }

        @Test
        void failsIfPageableNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllByMedicationTypeId("SYRUP", null));
        }
    }
}