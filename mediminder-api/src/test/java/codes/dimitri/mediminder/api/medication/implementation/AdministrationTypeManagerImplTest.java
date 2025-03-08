package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.AdministrationTypeDTO;
import codes.dimitri.mediminder.api.medication.AdministrationTypeManager;
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
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder"
})
@Transactional
@Sql("classpath:test-data/medication.sql")
@Sql(value = "classpath:test-data/cleanup-medication.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdministrationTypeManagerImplTest {
    @Autowired
    private AdministrationTypeManager manager;
    @MockitoBean
    private UserManager userManager;

    @Nested
    class findAllByMedicationTypeId {
        @Test
        void returnsResults() {
            var pageRequest = PageRequest.of(0, 10);
            var results = manager.findAllByMedicationTypeId("CAPSULE", pageRequest);
            assertThat(results).containsExactly(
                new AdministrationTypeDTO("BUCCAL", "Buccal (Between the gums and cheek)"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new AdministrationTypeDTO("SUBLINGUAL", "Sublingual (Under the tongue)")
            );
        }

        @Test
        void failsIfMedicationTypeNotGiven() {
            var pageRequest = PageRequest.of(0, 10);
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllByMedicationTypeId(null, pageRequest));
        }

        @Test
        void failsIfPageRequestNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllByMedicationTypeId("CAPSULE", null));
        }
    }
}