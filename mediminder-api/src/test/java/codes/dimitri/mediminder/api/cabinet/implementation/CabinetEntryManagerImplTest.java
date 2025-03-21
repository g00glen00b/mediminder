package codes.dimitri.mediminder.api.cabinet.implementation;

import codes.dimitri.mediminder.api.cabinet.*;
import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2"
})
@Transactional
@Sql("classpath:test-data/cabinet-entries.sql")
@Sql(value = "classpath:test-data/cleanup-cabinet-entries.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CabinetEntryManagerImplTest {
    @Autowired
    private CabinetEntryManager manager;
    @Autowired
    private CabinetEntryEntityRepository repository;
    @MockitoBean
    private UserManager userManager;
    @MockitoBean
    private MedicationManager medicationManager;

    @Nested
    class createForCurrentUser {
        @Test
        void returnsResult() {
            var medication = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var request = new CreateCabinetEntryRequestDTO(
                medication.id(),
                new BigDecimal("30"),
                LocalDate.of(2025, 10, 1)
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            CabinetEntryDTO result = manager.createForCurrentUser(request);
            assertThat(result).isEqualTo(new CabinetEntryDTO(
                result.id(),
                user.id(),
                medication,
                new BigDecimal("30"),
                LocalDate.of(2025, 10, 1)
            ));
        }

        @Test
        void savesEntity() {
            var medication = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var request = new CreateCabinetEntryRequestDTO(
                medication.id(),
                new BigDecimal("30"),
                LocalDate.of(2025, 10, 1)
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            CabinetEntryDTO result = manager.createForCurrentUser(request);
            CabinetEntryEntity entity = repository.findById(result.id()).orElseThrow();
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new CabinetEntryEntity(
                    result.id(),
                    user.id(),
                    medication.id(),
                    new BigDecimal("30"),
                    LocalDate.of(2025, 10, 1)
                ));
        }

        @Test
        void failsIfRemainingDosesMoreThanDosesMedication() {
            var medication = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var request = new CreateCabinetEntryRequestDTO(
                medication.id(),
                new BigDecimal("60"),
                LocalDate.of(2025, 10, 1)
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Remaining doses cannot be more than the initial doses per package");
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var medication = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var request = new CreateCabinetEntryRequestDTO(
                medication.id(),
                new BigDecimal("30"),
                LocalDate.of(2025, 10, 1)
            );
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfMedicationDoesNotExist() {
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var request = new CreateCabinetEntryRequestDTO(
                UUID.randomUUID(),
                new BigDecimal("30"),
                LocalDate.of(2025, 10, 1)
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdForCurrentUser(request.medicationId())).thenThrow(new MedicationNotFoundException(request.medicationId()));
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Medication is not found");
        }

        @Test
        void failsIfRequestNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(null));
        }

        @Test
        void failsIfMedicationIsNotGiven() {
            var request = new CreateCabinetEntryRequestDTO(
                null,
                new BigDecimal("30"),
                LocalDate.of(2025, 10, 1)
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Medication is required");
        }

        @Test
        void failsIfRemainingDosesNotGiven() {
            var request = new CreateCabinetEntryRequestDTO(
                UUID.randomUUID(),
                null,
                LocalDate.of(2025, 10, 1)
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("The amount of remaining doses is required");
        }

        @Test
        void failsIfRemainingDosesNegative() {
            var request = new CreateCabinetEntryRequestDTO(
                UUID.randomUUID(),
                new BigDecimal("-30"),
                LocalDate.of(2025, 10, 1)
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("The amount of remaining doses must be zero or positive");
        }

        @Test
        void failsIfExpiryDateNotGiven() {
            var request = new CreateCabinetEntryRequestDTO(
                UUID.randomUUID(),
                new BigDecimal("30"),
                null
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageContaining("Expiry date is required");
        }
    }

    @Nested
    class findAllForCurrentUser {
        @Test
        void returnsResults() {
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var pageRequest = PageRequest.of(0, 10);
            when(medicationManager.findByIdAndUserId(medication.id(), user.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            Page<CabinetEntryDTO> results = manager.findAllForCurrentUser(pageRequest);
            assertThat(results).containsExactly(
                new CabinetEntryDTO(
                    UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c"),
                    UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                    medication,
                    new BigDecimal("80"),
                    LocalDate.of(2024, 6, 30)
                )
            );
            verify(medicationManager).findByIdAndUserId(medication.id(), user.id());
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.findAllForCurrentUser(pageRequest))
                .withMessage("User is not authenticated");
        }

        @Test
        void setsEmptyMedicationIfNotFound() {
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenReturn(user);
            Page<CabinetEntryDTO> results = manager.findAllForCurrentUser(pageRequest);
            assertThat(results).containsExactly(
                new CabinetEntryDTO(
                    UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c"),
                    UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                    null,
                    new BigDecimal("80"),
                    LocalDate.of(2024, 6, 30)
                )
            );
        }

        @Test
        void failsIfPageableNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllForCurrentUser(null));
        }
    }

    @Nested
    class findByIdForCurrentUser {
        @Test
        void returnsResult() {
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            UUID id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(medicationManager.findByIdAndUserId(medication.id(), user.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            CabinetEntryDTO result = manager.findByIdForCurrentUser(id);
            assertThat(result).isEqualTo(new CabinetEntryDTO(
                id,
                user.id(),
                medication,
                new BigDecimal("80"),
                LocalDate.of(2024, 6, 30)
            ));
        }

        @Test
        void failsIfUserNotAuthenticated() {
            UUID id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(id))
                .withMessage("User is not authenticated");
        }

        @Test
        void returnsNoMedicationIfNotFound() {
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            UUID id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(userManager.findCurrentUser()).thenReturn(user);
            CabinetEntryDTO result = manager.findByIdForCurrentUser(id);
            assertThat(result).isEqualTo(new CabinetEntryDTO(
                id,
                user.id(),
                null,
                new BigDecimal("80"),
                LocalDate.of(2024, 6, 30)
            ));
        }

        @Test
        void failsIfNotFound() {
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            UUID id = UUID.fromString("1571fd59-d40d-4db1-8739-8830bc67516f");
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(CabinetEntryNotFoundException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(id))
                .withMessage("Cabinet entry with ID '1571fd59-d40d-4db1-8739-8830bc67516f' does not exist");
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(null));
        }
    }

    @Nested
    class updateForCurrentUser {
        @Test
        void returnsResult() {
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("10"),
                LocalDate.of(2025, 6, 30)
            );
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            CabinetEntryDTO result = manager.updateForCurrentUser(id, request);
            assertThat(result).isEqualTo(new CabinetEntryDTO(
               id,
               user.id(),
               medication,
               new BigDecimal("10"),
               LocalDate.of(2025, 6, 30)
            ));
        }

        @Test
        void failsIfRemainingDosesMoreThanDosesMedication() {
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("60"),
                LocalDate.of(2025, 6, 30)
            );
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("50"),
                Color.RED
            );
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Remaining doses cannot be more than the initial doses per package");
        }

        @Test
        void failsIfMedicationNotFound() {
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("10"),
                LocalDate.of(2025, 6, 30)
            );
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            var medicationid = UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea");
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdForCurrentUser(medicationid)).thenThrow(new MedicationNotFoundException(medicationid));
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Medication is not found");
        }

        @Test
        void savesEntity() {
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("10"),
                LocalDate.of(2025, 6, 30)
            );
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            manager.updateForCurrentUser(id, request);
            CabinetEntryEntity entity = repository.findById(id).orElseThrow();
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new CabinetEntryEntity(
                    id,
                    user.id(),
                    medication.id(),
                    new BigDecimal("10"),
                    LocalDate.of(2025, 6, 30)
                ));
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("10"),
                LocalDate.of(2025, 6, 30)
            );
            var medication = new MedicationDTO(
                UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfEntityDoesNotExist() {
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("10"),
                LocalDate.of(2025, 6, 30)
            );
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var id = UUID.fromString("1571fd59-d40d-4db1-8739-8830bc67516f");
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(CabinetEntryNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Cabinet entry with ID '1571fd59-d40d-4db1-8739-8830bc67516f' does not exist");
        }

        @Test
        void failsIfIdNotGiven() {
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("10"),
                LocalDate.of(2025, 6, 30)
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(null, request));
        }

        @Test
        void failsIfRequestNotGiven() {
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, null));
        }

        @Test
        void failsIfRemainingDosesNotGiven() {
            var request = new UpdateCabinetEntryRequestDTO(
                null,
                LocalDate.of(2025, 6, 30)
            );
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("The amount of remaining doses is required");
        }

        @Test
        void failsIfRemainingDosesNegative() {
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("-10"),
                LocalDate.of(2025, 6, 30)
            );
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("The amount of remaining doses must be zero or positive");
        }

        @Test
        void failsIfExpiryDateNotGiven() {
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("10"),
                null
            );
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("ec544543-9aff-4172-989d-ebd5d08a0dea"),
                "Dafalgan 1g",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(medicationManager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Expiry date is required");
        }
    }

    @Nested
    class deleteForCurrentUser {
        @Test
        void deletesEntity() {
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(userManager.findCurrentUser()).thenReturn(user);
            manager.deleteForCurrentUser(id);
            assertThat(repository.existsById(id)).isFalse();
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var id = UUID.fromString("b7cfa15e-1fe5-44b1-913b-98a7a0018d6c");
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(id))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfEntityDoesNotExist() {
            var user = new UserDTO(
                UUID.fromString("ed9e7a22-ebe1-4627-929d-e63f174cf6af"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var id = UUID.fromString("1571fd59-d40d-4db1-8739-8830bc67516f");
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(CabinetEntryNotFoundException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(id))
                .withMessage("Cabinet entry with ID '1571fd59-d40d-4db1-8739-8830bc67516f' does not exist");
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(null));
        }
    }

    @Nested
    class calculateTotalRemainingDosesByMedicationId {
        @ParameterizedTest
        @CsvSource({
            "bdeb432c-c1d7-4482-ae55-19c2750b7796,10",
            "65729ae5-a7b9-40a0-8299-ba26a6f05745,60",
            "00000000-0000-0000-0000-000000000000,0"
        })
        void returnsResult(String id, BigDecimal expected) {
            var medicationId = UUID.fromString(id);
            BigDecimal result = manager.calculateTotalRemainingDosesByMedicationId(medicationId);
            assertThat(result).isEqualTo(expected);
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.calculateTotalRemainingDosesByMedicationId(null));
        }
    }

    @Nested
    class deleteAllByMedicationId {
        @ParameterizedTest
        @CsvSource({
            "bdeb432c-c1d7-4482-ae55-19c2750b7796,eaf1d029-d072-4554-8734-914bc4d7cb07,2",
            "ec544543-9aff-4172-989d-ebd5d08a0dea,ed9e7a22-ebe1-4627-929d-e63f174cf6af,0",
            "00000000-0000-0000-0000-000000000000,eaf1d029-d072-4554-8734-914bc4d7cb07,4"
        })
        void deletesEntities(UUID medicationId, UUID userId, int expectedSize) {
            var pageRequest = PageRequest.of(0, 10);
            manager.deleteAllByMedicationId(medicationId);
            assertThat(repository.findAllByUserId(userId, pageRequest)).hasSize(expectedSize);
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.deleteAllByMedicationId(null));
        }
    }

    @Nested
    class subtractDosesByMedicationId {
        @Test
        void subtractsDoses() {
            var medicationId = UUID.fromString("65729ae5-a7b9-40a0-8299-ba26a6f05745");
            var pageRequest = PageRequest.of(0, 10);
            manager.subtractDosesByMedicationId(medicationId, new BigDecimal("5"));
            Page<CabinetEntryEntity> results = repository.findAllByMedicationId(medicationId, pageRequest);
            assertThat(results)
                .extracting(CabinetEntryEntity::getId, CabinetEntryEntity::getRemainingDoses)
                .containsOnly(
                    tuple(UUID.fromString("dc99854f-8417-47af-81a5-15f22a3bd64c"), new BigDecimal("15")),
                    tuple(UUID.fromString("1571fd59-d40d-4db1-8739-8830bc67516f"), new BigDecimal("40")));
        }

        @Test
        void rollsOverToOtherEntriesIfNecessary() {
            var medicationId = UUID.fromString("65729ae5-a7b9-40a0-8299-ba26a6f05745");
            var pageRequest = PageRequest.of(0, 10);
            manager.subtractDosesByMedicationId(medicationId, new BigDecimal("30"));
            Page<CabinetEntryEntity> results = repository.findAllByMedicationId(medicationId, pageRequest);
            assertThat(results)
                .extracting(CabinetEntryEntity::getId, CabinetEntryEntity::getRemainingDoses)
                .containsOnly(
                    tuple(UUID.fromString("1571fd59-d40d-4db1-8739-8830bc67516f"), new BigDecimal("30")));
        }

        @Test
        void deletesAllEntriesIfHigherThanAvailable() {
            var medicationId = UUID.fromString("65729ae5-a7b9-40a0-8299-ba26a6f05745");
            var pageRequest = PageRequest.of(0, 10);
            manager.subtractDosesByMedicationId(medicationId, new BigDecimal("100"));
            Page<CabinetEntryEntity> results = repository.findAllByMedicationId(medicationId, pageRequest);
            assertThat(results).isEmpty();
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.subtractDosesByMedicationId(null, new BigDecimal("5")));
        }

        @Test
        void failsIfDosesNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.subtractDosesByMedicationId(UUID.randomUUID(), null));
        }

        @Test
        void failsIfDosesNegative() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.subtractDosesByMedicationId(UUID.randomUUID(), new BigDecimal("-5")));
        }
    }

    @Nested
    class addDosesByMedicationId {
        @Test
        void addsDosesToOldestEntity() {
            var medicationId = UUID.fromString("65729ae5-a7b9-40a0-8299-ba26a6f05745");
            var pageRequest = PageRequest.of(0, 10);
            manager.addDosesByMedicationId(medicationId, new BigDecimal("5"));
            Page<CabinetEntryEntity> results = repository.findAllByMedicationId(medicationId, pageRequest);
            assertThat(results)
                .extracting(CabinetEntryEntity::getId, CabinetEntryEntity::getRemainingDoses)
                .containsOnly(
                    tuple(UUID.fromString("dc99854f-8417-47af-81a5-15f22a3bd64c"), new BigDecimal("25")),
                    tuple(UUID.fromString("1571fd59-d40d-4db1-8739-8830bc67516f"), new BigDecimal("40")));
        }

        @Test
        void doesntDoAnythingIfNoEntriesExist() {
            var medicationId = UUID.fromString("00000000-0000-0000-0000-000000000000");
            var pageRequest = PageRequest.of(0, 10);
            manager.addDosesByMedicationId(medicationId, new BigDecimal("5"));
            Page<CabinetEntryEntity> results = repository.findAllByMedicationId(medicationId, pageRequest);
            assertThat(results).isEmpty();
        }

        @Test
        void failsIfMedicationIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.addDosesByMedicationId(null, new BigDecimal("5")));
        }

        @Test
        void failsIfDosesNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.addDosesByMedicationId(UUID.randomUUID(), null));
        }

        @Test
        void failsIfDosesNegative() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.addDosesByMedicationId(UUID.randomUUID(), new BigDecimal("-5")));
        }
    }

    @Nested
    class findAllNonEmptyWithExpiryDateBefore {
        @ParameterizedTest
        @CsvSource({
            "2024-06-30,4",
            "2024-06-29,3",
            "2024-06-28,1",
            "2024-06-01,0"
        })
        void returnsResults(LocalDate targetDate, int expectedSize) {
            var pageRequest = PageRequest.of(0, 10);
            Page<CabinetEntryDTO> results = manager.findAllNonEmptyWithExpiryDateBefore(targetDate, pageRequest);
            assertThat(results).hasSize(expectedSize);
        }

        @Test
        void failsIfTargetDateNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllNonEmptyWithExpiryDateBefore(null, PageRequest.of(0, 10)));
        }

        @Test
        void failsIfPageableNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllNonEmptyWithExpiryDateBefore(LocalDate.now(), null));
        }
    }
}