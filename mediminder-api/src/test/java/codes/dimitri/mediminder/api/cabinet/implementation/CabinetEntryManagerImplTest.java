package codes.dimitri.mediminder.api.cabinet.implementation;

import codes.dimitri.mediminder.api.cabinet.*;
import codes.dimitri.mediminder.api.medication.MedicationDTO;
import codes.dimitri.mediminder.api.medication.MedicationManager;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {CabinetEntryManagerImpl.class, CabinetEntryMapperImpl.class})
@ContextConfiguration(classes = ValidationAutoConfiguration.class)
class CabinetEntryManagerImplTest {
    @Autowired
    private CabinetEntryManagerImpl manager;
    @MockBean
    private UserManager userManager;
    @MockBean
    private MedicationManager medicationManager;
    @MockBean
    private CabinetEntryEntityRepository repository;
    @Captor
    private ArgumentCaptor<CabinetEntryEntity> anyEntity;

    @Nested
    class create {
        @Test
        void savesEntity() {
            // Given
            var medication = Instancio.of(MedicationDTO.class)
                .set(field(MedicationDTO::dosesPerPackage), new BigDecimal("100"))
                .create();
            var user = Instancio.create(UserDTO.class);
            var request = Instancio.of(CreateCabinetEntryRequestDTO.class)
                .set(field(CreateCabinetEntryRequestDTO::medicationId), medication.id())
                .set(field(CreateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("30"))
                .create();
            // When
            when(repository.save(any())).thenAnswer(returnsFirstArg());
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            // Then
            manager.createForCurrentUser(request);
            verify(userManager).findCurrentUser();
            verify(medicationManager).findByIdForCurrentUser(request.medicationId());
            verify(repository).save(anyEntity.capture());
            assertThat(anyEntity.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(new CabinetEntryEntity(
                    user.id(),
                    medication.id(),
                    request.remainingDoses(),
                    request.expiryDate()
                ));
        }

        @Test
        void returnsDTO() {
            // Given
            var medication = Instancio.of(MedicationDTO.class)
                .set(field(MedicationDTO::dosesPerPackage), new BigDecimal("100"))
                .create();
            var user = Instancio.create(UserDTO.class);
            var request = Instancio.of(CreateCabinetEntryRequestDTO.class)
                .set(field(CreateCabinetEntryRequestDTO::medicationId), medication.id())
                .set(field(CreateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("30"))
                .create();
            // When
            when(repository.save(any())).thenAnswer(returnsFirstArg());
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            // Then
            CabinetEntryDTO result = manager.createForCurrentUser(request);
            verify(repository).save(anyEntity.capture());
            assertThat(result).isEqualTo(new CabinetEntryDTO(
                anyEntity.getValue().getId(),
                user.id(),
                medication,
                request.remainingDoses(),
                request.expiryDate()
            ));
        }

        @Test
        void doesNotSaveWhenMedicationNotGiven() {
            // Given
            var request = Instancio.of(CreateCabinetEntryRequestDTO.class)
                .ignore(field(CreateCabinetEntryRequestDTO::medicationId))
                .set(field(CreateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("30"))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Medication is required");
            verifyNoInteractions(repository);
        }

        @Test
        void doesNotSaveWhenRemainingDosesNotGiven() {
            // Given
            var request = Instancio.of(CreateCabinetEntryRequestDTO.class)
                .ignore(field(CreateCabinetEntryRequestDTO::remainingDoses))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("The amount of remaining doses is required");
            verifyNoInteractions(repository);
        }

        @Test
        void doesNotSaveWhenRemainingDosesIsNegative() {
            // Given
            var request = Instancio.of(CreateCabinetEntryRequestDTO.class)
                .set(field(CreateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("-10"))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("The amount of remaining doses must be zero or positive");
            verifyNoInteractions(repository);
        }

        @Test
        void doesNotSaveWhenExpiryDateNotGiven() {
            // Given
            var request = Instancio.of(CreateCabinetEntryRequestDTO.class)
                .ignore(field(CreateCabinetEntryRequestDTO::expiryDate))
                .set(field(CreateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("30"))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Expiry date is required");
            verifyNoInteractions(repository);
        }

        @Test
        void doesNotSaveWhenUserNotFound() {
            // Given
            var request = Instancio.create(CreateCabinetEntryRequestDTO.class);
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("User is not authenticated");
            verifyNoInteractions(repository);
        }

        @Test
        void doesNotSaveWhenMedicationNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var request = Instancio.create(CreateCabinetEntryRequestDTO.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Medication is not found");
            verifyNoInteractions(repository);
        }

        @Test
        void doesNotSaveWhenRemainingDosesIsLargerThanInitialDosesMedication() {
            // Given
            var medication = Instancio.of(MedicationDTO.class)
                .set(field(MedicationDTO::dosesPerPackage), new BigDecimal("100"))
                .create();
            var user = Instancio.create(UserDTO.class);
            var request = Instancio.of(CreateCabinetEntryRequestDTO.class)
                .set(field(CreateCabinetEntryRequestDTO::medicationId), medication.id())
                .set(field(CreateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("200"))
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Remaining doses cannot be more than the initial doses per package");
            verifyNoInteractions(repository);
        }
    }

    @Nested
    class findAll {
        @Test
        void returnsDTO() {
            // Given
            var medication = Instancio.create(MedicationDTO.class);
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getUserId), user.id())
                .set(field(CabinetEntryEntity::getMedicationId), medication.id())
                .create();
            var pageRequest = PageRequest.of(0, 10);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            when(repository.findAllByUserId(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
            // Then
            Page<CabinetEntryDTO> result = manager.findAllForCurrentUser(pageRequest);
            verify(medicationManager).findByIdForCurrentUser(entity.getMedicationId());
            verify(userManager).findCurrentUser();
            verify(repository).findAllByUserId(user.id(), pageRequest);
            assertThat(result).isEqualTo(new PageImpl<>(List.of(
                new CabinetEntryDTO(
                    entity.getId(),
                    user.id(),
                    medication,
                    entity.getRemainingDoses(),
                    entity.getExpiryDate()
                )
            )));
        }

        @Test
        void returnsEmptyMedicationIfNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getUserId), user.id())
                .create();
            var pageRequest = PageRequest.of(0, 10);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(repository.findAllByUserId(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
            // Then
            Page<CabinetEntryDTO> result = manager.findAllForCurrentUser(pageRequest);
            verify(medicationManager).findByIdForCurrentUser(entity.getMedicationId());
            verify(userManager).findCurrentUser();
            verify(repository).findAllByUserId(user.id(), pageRequest);
            assertThat(result).isEqualTo(new PageImpl<>(List.of(
                new CabinetEntryDTO(
                    entity.getId(),
                    user.id(),
                    null,
                    entity.getRemainingDoses(),
                    entity.getExpiryDate()
                )
            )));
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var pageRequest = PageRequest.of(0, 10);
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.findAllForCurrentUser(pageRequest))
                .withMessage("User is not authenticated");
            verify(userManager).findCurrentUser();
        }
    }

    @Nested
    class findById {
        @Test
        void returnsDTO() {
            // Given
            var medication = Instancio.create(MedicationDTO.class);
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getUserId), user.id())
                .set(field(CabinetEntryEntity::getMedicationId), medication.id())
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            // Then
            CabinetEntryDTO result = manager.findByIdForCurrentUser(entity.getId());
            verify(medicationManager).findByIdForCurrentUser(entity.getMedicationId());
            verify(userManager).findCurrentUser();
            verify(repository).findByIdAndUserId(entity.getId(), user.id());
            assertThat(result).isEqualTo(new CabinetEntryDTO(
                entity.getId(),
                user.id(),
                medication,
                entity.getRemainingDoses(),
                entity.getExpiryDate()
            ));
        }

        @Test
        void returnsEmptyMedicationIfNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getUserId), user.id())
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            // Then
            CabinetEntryDTO result = manager.findByIdForCurrentUser(entity.getId());
            verify(medicationManager).findByIdForCurrentUser(entity.getMedicationId());
            verify(userManager).findCurrentUser();
            verify(repository).findByIdAndUserId(entity.getId(), user.id());
            assertThat(result).isEqualTo(new CabinetEntryDTO(
                entity.getId(),
                user.id(),
                null,
                entity.getRemainingDoses(),
                entity.getExpiryDate()
            ));
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var id = UUID.randomUUID();
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(id))
                .withMessage("User is not authenticated");
            verify(userManager).findCurrentUser();
        }

        @Test
        void throwsExceptionIfEntityNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var id = UUID.randomUUID();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(CabinetEntryNotFoundException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(id))
                .withMessage("Cabinet entry with ID '" + id + "' does not exist");
            verify(userManager).findCurrentUser();
            verify(repository).findByIdAndUserId(id, user.id());
        }
    }

    @Nested
    class update {
        @Test
        void returnsDTO() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.of(MedicationDTO.class)
                .set(field(MedicationDTO::dosesPerPackage), new BigDecimal("100"))
                .create();
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getUserId), user.id())
                .set(field(CabinetEntryEntity::getMedicationId), medication.id())
                .create();
            var request = Instancio.of(UpdateCabinetEntryRequestDTO.class)
                .set(field(UpdateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("50"))
                .create();
            // When
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            // Then
            CabinetEntryDTO result = manager.updateForCurrentUser(entity.getId(), request);
            assertThat(result).isEqualTo(new CabinetEntryDTO(
                entity.getId(),
                user.id(),
                medication,
                request.remainingDoses(),
                request.expiryDate()
            ));
        }

        @Test
        void savesEntity() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.of(MedicationDTO.class)
                .set(field(MedicationDTO::dosesPerPackage), new BigDecimal("100"))
                .create();
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getUserId), user.id())
                .set(field(CabinetEntryEntity::getMedicationId), medication.id())
                .create();
            var request = Instancio.of(UpdateCabinetEntryRequestDTO.class)
                .set(field(UpdateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("50"))
                .create();
            // When
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            // Then
            manager.updateForCurrentUser(entity.getId(), request);
            verify(userManager).findCurrentUser();
            verify(medicationManager).findByIdForCurrentUser(entity.getMedicationId());
            verify(repository).findByIdAndUserId(entity.getId(), user.id());
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new CabinetEntryEntity(
               entity.getId(),
               user.id(),
               medication.id(),
               request.remainingDoses(),
               request.expiryDate()
            ));
        }

        @Test
        void throwsExceptionIfRemainingDosesNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateCabinetEntryRequestDTO.class)
                .ignore(field(UpdateCabinetEntryRequestDTO::remainingDoses))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("The amount of remaining doses is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfRemainingDosesNegative() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateCabinetEntryRequestDTO.class)
                .set(field(UpdateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("-10"))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("The amount of remaining doses must be zero or positive");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfExpiryDateNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateCabinetEntryRequestDTO.class)
                .set(field(UpdateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("30"))
                .ignore(field(UpdateCabinetEntryRequestDTO::expiryDate))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Expiry date is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfUnauthenticated() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateCabinetEntryRequestDTO.class)
                .set(field(UpdateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("50"))
                .create();
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("User is not authenticated");
            verify(userManager).findCurrentUser();
        }

        @Test
        void throwsExceptionIfEntityNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateCabinetEntryRequestDTO.class)
                .set(field(UpdateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("50"))
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(CabinetEntryNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Cabinet entry with ID '" + id + "' does not exist");
            verify(userManager).findCurrentUser();
            verify(repository).findByIdAndUserId(id, user.id());
        }

        @Test
        void throwsExceptionIfMedicationNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getUserId), user.id())
                .create();
            var request = Instancio.of(UpdateCabinetEntryRequestDTO.class)
                .set(field(UpdateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("50"))
                .create();
            // When
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(entity.getId(), request))
                .withMessage("Medication is not found");
            verify(repository).findByIdAndUserId(entity.getId(), user.id());
            verify(userManager).findCurrentUser();
            verify(medicationManager).findByIdForCurrentUser(entity.getMedicationId());
        }

        @Test
        void throwsExceptionIfDosesAreNotValid() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medication = Instancio.of(MedicationDTO.class)
                .set(field(MedicationDTO::dosesPerPackage), new BigDecimal("100"))
                .create();
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getUserId), user.id())
                .set(field(CabinetEntryEntity::getMedicationId), medication.id())
                .create();
            var request = Instancio.of(UpdateCabinetEntryRequestDTO.class)
                .set(field(UpdateCabinetEntryRequestDTO::remainingDoses), new BigDecimal("200"))
                .create();
            // When
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(entity.getId(), request))
                .withMessage("Remaining doses cannot be more than the initial doses per package");
            verify(repository).findByIdAndUserId(entity.getId(), user.id());
            verify(userManager).findCurrentUser();
            verify(medicationManager).findByIdForCurrentUser(medication.id());
        }
    }

    @Nested
    class delete {
        @Test
        void deletesEntity() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getUserId), user.id())
                .create();
            // When
            when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            manager.deleteForCurrentUser(entity.getId());
            verify(repository).findByIdAndUserId(entity.getId(), user.id());
            verify(userManager).findCurrentUser();
            verify(repository).delete(entity);
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var id = UUID.randomUUID();
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(id))
                .withMessage("User is not authenticated");
            verify(userManager).findCurrentUser();
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfEntityNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var id = UUID.randomUUID();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(CabinetEntryNotFoundException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(id))
                .withMessage("Cabinet entry with ID '" + id + "' does not exist");
            verify(repository).findByIdAndUserId(id, user.id());
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    class calculateTotalRemainingDosesByMedicationId {
        @Test
        void returnsResult() {
            // Given
            var medicationId = UUID.randomUUID();
            var remainingDoses = new BigDecimal("100");
            // When
            when(repository.sumRemainingDosesByMedicationId(any())).thenReturn(remainingDoses);
            // Then
            BigDecimal result = manager.calculateTotalRemainingDosesByMedicationId(medicationId);
            assertThat(result).isEqualTo(remainingDoses);
            verify(repository).sumRemainingDosesByMedicationId(medicationId);
        }

        @Test
        void returnsEmptyResultIfNotFound() {
            // Given
            var medicationId = UUID.randomUUID();
            // Then
            BigDecimal result = manager.calculateTotalRemainingDosesByMedicationId(medicationId);
            assertThat(result).isZero();
        }
    }

    @Test
    void deleteAllByMedicationId() {
        // Given
        var medicationId = UUID.randomUUID();
        // Then
        manager.deleteAllByMedicationId(medicationId);
        verify(repository).deleteAllByMedicationId(medicationId);
    }

    @Nested
    class subtractDosesByMedicationId {
        @Test
        void returnsRemainingResult() {
            // Given
            var medicationId = UUID.randomUUID();
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getMedicationId), medicationId)
                .set(field(CabinetEntryEntity::getRemainingDoses), new BigDecimal("100"))
                .create();
            // When
            when(repository.findAllWithRemainingDosesByMedicationId(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));
            // Then
            manager.subtractDosesByMedicationId(medicationId, new BigDecimal("10"));
            assertThat(entity.getRemainingDoses()).isEqualTo("90");
            verify(repository).findAllWithRemainingDosesByMedicationId(medicationId, PageRequest.of(0, 20, Sort.Direction.ASC, "expiryDate"));
        }

        @Test
        void subtractsFromMultipleEntitiesIfNecessary() {
            // Given
            var medicationId = UUID.randomUUID();
            var entity1 = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getMedicationId), medicationId)
                .set(field(CabinetEntryEntity::getRemainingDoses), new BigDecimal("5"))
                .create();
            var entity2 = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getMedicationId), medicationId)
                .set(field(CabinetEntryEntity::getRemainingDoses), new BigDecimal("10"))
                .create();
            // When
            when(repository.findAllWithRemainingDosesByMedicationId(any(), any())).thenReturn(new PageImpl<>(List.of(entity1, entity2)));
            // Then
            manager.subtractDosesByMedicationId(medicationId, new BigDecimal("10"));
            assertThat(entity1.getRemainingDoses()).isZero();
            assertThat(entity2.getRemainingDoses()).isEqualTo("5");
            verify(repository).findAllWithRemainingDosesByMedicationId(medicationId, PageRequest.of(0, 20, Sort.Direction.ASC, "expiryDate"));
            verify(repository).deleteAll(List.of(entity1));
        }

        @Test
        void throwsExceptionIfNotEnoughRemainingDoses() {
            // Given
            var medicationId = UUID.randomUUID();
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getMedicationId), medicationId)
                .set(field(CabinetEntryEntity::getRemainingDoses), new BigDecimal("5"))
                .create();
            // When
            when(repository.findAllWithRemainingDosesByMedicationId(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.subtractDosesByMedicationId(medicationId, new BigDecimal("10")))
                .withMessage("There are not enough available doses in your cabinet");
            verify(repository).findAllWithRemainingDosesByMedicationId(medicationId, PageRequest.of(0, 20, Sort.Direction.ASC, "expiryDate"));
        }

        @Test
        void throwsExceptionIfNotEnoughEntries() {
            // Given
            var medicationId = UUID.randomUUID();
            // When
            when(repository.findAllWithRemainingDosesByMedicationId(any(), any())).thenReturn(new PageImpl<>(List.of()));
            // Then
            assertThatExceptionOfType(InvalidCabinetEntryException.class)
                .isThrownBy(() -> manager.subtractDosesByMedicationId(medicationId, new BigDecimal("10")))
                .withMessage("There are not enough available doses in your cabinet");
            verify(repository).findAllWithRemainingDosesByMedicationId(medicationId, PageRequest.of(0, 20, Sort.Direction.ASC, "expiryDate"));
        }
    }

    @Nested
    class addDosesByMedicationId {
        @Test
        void increasesRemainingDoses() {
            // Given
            var medicationId = UUID.randomUUID();
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getMedicationId), medicationId)
                .set(field(CabinetEntryEntity::getRemainingDoses), new BigDecimal("5"))
                .create();
            // When
            when(repository.findAllByMedicationId(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));
            // Then
            manager.addDosesByMedicationId(medicationId, new BigDecimal("5"));
            assertThat(entity.getRemainingDoses()).isEqualTo(new BigDecimal("10"));
            verify(repository).findAllByMedicationId(medicationId, PageRequest.of(0, 1, Sort.Direction.ASC, "expiryDate"));
        }

        @Test
        void doesNotIncreaseRemainingDosesIfNoEntityFound() {
            // Given
            var medicationId = UUID.randomUUID();
            // When
            when(repository.findAllByMedicationId(any(), any())).thenReturn(new PageImpl<>(List.of()));
            // Then
            manager.addDosesByMedicationId(medicationId, new BigDecimal("5"));
            verify(repository).findAllByMedicationId(medicationId, PageRequest.of(0, 1, Sort.Direction.ASC, "expiryDate"));
        }
    }

    @Nested
    class findAllNonEmptyWithExpiryDateBefore {
        @Test
        void returnsDTO() {
            // Given
            var date = LocalDate.now();
            var pageRequest = PageRequest.of(0, 10);
            var medication = Instancio.create(MedicationDTO.class);
            var entity = Instancio.of(CabinetEntryEntity.class)
                .set(field(CabinetEntryEntity::getMedicationId), medication.id())
                .create();
            // When
            when(repository.findAllWithRemainingDosesWithExpiryDateBefore(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));
            when(medicationManager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
            // Then
            Page<CabinetEntryDTO> result = manager.findAllNonEmptyWithExpiryDateBefore(date, pageRequest);
            assertThat(result.getContent()).contains(new CabinetEntryDTO(
                entity.getId(),
                entity.getUserId(),
                medication,
                entity.getRemainingDoses(),
                entity.getExpiryDate()
            ));
            verify(repository).findAllWithRemainingDosesWithExpiryDateBefore(date, pageRequest);
            verify(medicationManager).findByIdForCurrentUser(medication.id());
        }

        @Test
        void returnsDTOWithoutMedication() {
            // Given
            var date = LocalDate.now();
            var pageRequest = PageRequest.of(0, 10);
            var entity = Instancio.create(CabinetEntryEntity.class);
            // When
            when(repository.findAllWithRemainingDosesWithExpiryDateBefore(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));
            // Then
            Page<CabinetEntryDTO> result = manager.findAllNonEmptyWithExpiryDateBefore(date, pageRequest);
            assertThat(result.getContent()).contains(new CabinetEntryDTO(
                entity.getId(),
                entity.getUserId(),
                null,
                entity.getRemainingDoses(),
                entity.getExpiryDate()
            ));
            verify(repository).findAllWithRemainingDosesWithExpiryDateBefore(date, pageRequest);
            verify(medicationManager).findByIdForCurrentUser(entity.getMedicationId());
        }
    }
}