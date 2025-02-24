package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder"
})
@Transactional
@Sql("classpath:test-data/medication.sql")
@Sql(value = "classpath:test-data/cleanup-medication.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@RecordApplicationEvents
class MedicationManagerImplTest {
    @Autowired
    private MedicationManagerImpl manager;
    @Autowired
    private MedicationEntityRepository repository;
    @Autowired
    private MedicationTypeEntityRepository medicationTypeEntityRepository;
    @Autowired
    private AdministrationTypeEntityRepository administrationTypeEntityRepository;
    @Autowired
    private DoseTypeEntityRepository doseTypeEntityRepository;
    @Autowired
    private ApplicationEvents events;
    @MockitoBean
    private UserManager userManager;

    @Nested
    class createForCurrentUser {
        @Test
        void returnsDto() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "CAPSULE",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            MedicationDTO result = manager.createForCurrentUser(request);
            assertThat(result).isEqualTo(new MedicationDTO(
                result.id(),
                "Hydrocortisone 14mg",
                new MedicationTypeDTO("CAPSULE", "Capsule"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("CAPSULE", "capsule(s)"),
                new BigDecimal("60"),
                Color.YELLOW
            ));
        }

        @Test
        void savesEntity() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "CAPSULE",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            MedicationDTO result = manager.createForCurrentUser(request);
            MedicationEntity entity = repository.findByIdAndUserId(result.id(), user.id()).orElseThrow();
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new MedicationEntity(
                    result.id(),
                    user.id(),
                    "Hydrocortisone 14mg",
                    medicationTypeEntityRepository.findById("CAPSULE").orElseThrow(),
                    administrationTypeEntityRepository.findById("ORAL").orElseThrow(),
                    doseTypeEntityRepository.findById("CAPSULE").orElseThrow(),
                    new BigDecimal("60"),
                    Color.YELLOW
            ));
        }

        @Test
        void failsIfNoCurrentUser() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "CAPSULE",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            assertThatExceptionOfType(InvalidMedicationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfInvalidMedicationType() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "DOESNOTEXIST",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(MedicationTypeNotFoundException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Medication type with ID 'DOESNOTEXIST' does not exist");
        }

        @Test
        void failsIfInvalidAdministrationType() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "CAPSULE",
                "DOESNOTEXIST",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(AdministrationTypeNotFoundException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Administration type with ID 'DOESNOTEXIST' does not exist");
        }

        @Test
        void failsIfInvalidDoseType() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "CAPSULE",
                "ORAL",
                "DOESNOTEXIST",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(DoseTypeNotFoundException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Dose type with ID 'DOESNOTEXIST' does not exist");
        }

        @Test
        void failsIfNameNotGiven() {
            var request = new CreateMedicationRequestDTO(
                "",
                "CAPSULE",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Name is required");
        }

        @Test
        void failsIfNameTooLong() {
            var request = new CreateMedicationRequestDTO(
                "A very long name that is more than one hundred and twenty eight characters so that the validation of the name length can be tested",
                "CAPSULE",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Name cannot contain more than 128 characters");
        }

        @Test
        void failsIfMedicationTypeMissing() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                null,
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Medication type is required");
        }

        @Test
        void failsIfAdministrationTypeMissing() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "CAPSULE",
                null,
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Administration type is required");
        }

        @Test
        void failsIfDoseTypeMissing() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "CAPSULE",
                "ORAL",
                null,
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Dose type is required");
        }

        @Test
        void failsIfDosesPerPackageIsMissing() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "CAPSULE",
                "ORAL",
                "CAPSULE",
                null,
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("The amount of doses per package is required");
        }

        @Test
        void failsIfDosesPerPackageIsNegative() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "CAPSULE",
                "ORAL",
                "CAPSULE",
                new BigDecimal("-10"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("The amount of doses per package must be zero or positive");
        }

        @Test
        void failsIfColorIsMissing() {
            var request = new CreateMedicationRequestDTO(
                "Hydrocortisone 14mg",
                "CAPSULE",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                null
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("The color is required");
        }
    }

    @Nested
    class findAllForCurrentUser {
        @Test
        void returnsAllMedication() {
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThat(manager.findAllForCurrentUser(null, pageRequest))
                .extracting(MedicationDTO::name)
                .containsExactly("Dafalgan 1g (100)", "Dafalgan 1g (50)", "Ibuprofen 400mg");
        }

        @Test
        void returnsMedicationMatchingSearchTerm() {
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThat(manager.findAllForCurrentUser("ibu", pageRequest))
                .extracting(MedicationDTO::name)
                .containsExactly("Ibuprofen 400mg");
        }

        @Test
        void failsIfNoCurrentUser() {
            var pageRequest = PageRequest.of(0, 10);
            assertThatExceptionOfType(InvalidMedicationException.class)
                .isThrownBy(() -> manager.findAllForCurrentUser(null, pageRequest))
                .withMessage("User is not authenticated");
        }
    }

    @Nested
    class findByIdForCurrentUser {
        @Test
        void returnsMedication() {
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            MedicationDTO result = manager.findByIdForCurrentUser(id).orElseThrow();
            assertThat(result.name()).isEqualTo("Dafalgan 1g (100)");
        }

        @Test
        void returnsNothingIfNotFound() {
            var id = UUID.fromString("4579fa76-1edc-4113-b521-2167713a3636");
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThat(manager.findByIdForCurrentUser(id)).isEmpty();
        }

        @Test
        void failsIfNoCurrentUser() {
            var id = UUID.fromString("4579fa76-1edc-4113-b521-2167713a3636");
            assertThatExceptionOfType(InvalidMedicationException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(id))
                .withMessage("User is not authenticated");
        }
    }

    @Nested
    class findByIdAndUserId {
        @Test
        void returnsMedication() {
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            var userId = UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124");
            MedicationDTO result = manager.findByIdAndUserId(id, userId).orElseThrow();
            assertThat(result.name()).isEqualTo("Dafalgan 1g (100)");
        }

        @Test
        void returnsNothingIfNotFound() {
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            var userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
            assertThat(manager.findByIdAndUserId(id, userId)).isEmpty();
        }
    }

    @Nested
    class deleteByIdForCurrentUser {
        @Test
        void deletes() {
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            manager.deleteByIdForCurrentUser(id);
            assertThat(repository.existsById(id)).isFalse();
        }

        @Test
        void failsIfIdUserNotFound() {
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            assertThatExceptionOfType(InvalidMedicationException.class)
                .isThrownBy(() -> manager.deleteByIdForCurrentUser(id))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfNotFound() {
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            var user = new UserDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(MedicationNotFoundException.class)
                .isThrownBy(() -> manager.deleteByIdForCurrentUser(id))
                .withMessage("Medication with ID '3257ee2d-b6c6-4a12-990e-826a80c43f17' does not exist");
        }

        @Test
        void publishesAnEvent() {
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            manager.deleteByIdForCurrentUser(id);
            var event = events.stream(MedicationDeletedEvent.class).findAny();
            assertThat(event).contains(new MedicationDeletedEvent(id));
        }
    }

    @Nested
    class updateForCurrentUser {
        @Test
        void returnsDto() {
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan 1g (50)",
                "ORAL",
                "TABLET",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            MedicationDTO result = manager.updateForCurrentUser(id, request);
            assertThat(result).isEqualTo(new MedicationDTO(
                id,
                "Dafalgan 1g (50)",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("60"),
                Color.YELLOW
            ));
        }

        @Test
        void savesEntity() {
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan 1g (50)",
                "ORAL",
                "TABLET",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            manager.updateForCurrentUser(id, request);
            assertThat(repository.findById(id).orElseThrow())
                .usingRecursiveComparison()
                .isEqualTo(new MedicationEntity(
                id,
                user.id(),
                "Dafalgan 1g (50)",
                medicationTypeEntityRepository.findById("TABLET").orElseThrow(),
                administrationTypeEntityRepository.findById("ORAL").orElseThrow(),
                doseTypeEntityRepository.findById("TABLET").orElseThrow(),
                new BigDecimal("60"),
                Color.YELLOW
            ));
        }

        @Test
        void failsIfAdministrationTypeMissing() {
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan 1g (50)",
                "SUBCUTANEOUS",
                "TABLET",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(AdministrationTypeNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Administration type with ID 'SUBCUTANEOUS' does not exist");
        }

        @Test
        void failsIfDoseTypeMissing() {
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan 1g (50)",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(DoseTypeNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Dose type with ID 'CAPSULE' does not exist");
        }

        @Test
        void failsIfNoCurrentUser() {
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan 1g (50)",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            assertThatExceptionOfType(InvalidMedicationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfMedicationDoesNotExist() {
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan 1g (50)",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("4579fa76-1edc-4113-b521-2167713a3636");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(MedicationNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Medication with ID '4579fa76-1edc-4113-b521-2167713a3636' does not exist");
        }

        @Test
        void failsIfNameNotGiven() {
            var request = new UpdateMedicationRequestDTO(
                null,
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Name is required");
        }

        @Test
        void failsIfNameTooLong() {
            var request = new UpdateMedicationRequestDTO(
                "A very long name that does not fit within the limit of one hundred and twenty eight characters so that we can test the constraint violation",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Name cannot contain more than 128 characters");
        }

        @Test
        void failsIfAdministrationTypeNotGiven() {
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan 1g (50)",
                null,
                "CAPSULE",
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Administration type is required");
        }

        @Test
        void failsIfDoseTYpeNotGiven() {
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan 1g (50)",
                "ORAL",
                null,
                new BigDecimal("60"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Dose type is required");
        }

        @Test
        void failsIfDoseNotGiven() {
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan 1g (50)",
                "ORAL",
                "CAPSULE",
                null,
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("The amount of doses per package is required");
        }

        @Test
        void failsIfDoseNegative() {
            var request = new UpdateMedicationRequestDTO(
                null,
                "ORAL",
                "CAPSULE",
                new BigDecimal("-10"),
                Color.YELLOW
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("The amount of doses per package must be zero or positive");
        }

        @Test
        void failsIfColorNotGiven() {
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan 1g (50)",
                "ORAL",
                "CAPSULE",
                new BigDecimal("60"),
                null
            );
            var user = new UserDTO(
                UUID.fromString("2e4aadf4-6d7e-4bd1-ad9f-87b26eb64124"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("3257ee2d-b6c6-4a12-990e-826a80c43f17");
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("The color is required");
        }
    }
}