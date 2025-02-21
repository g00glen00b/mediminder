package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {MedicationManagerImpl.class, MedicationEntityMapperImpl.class})
@ContextConfiguration(classes = ValidationAutoConfiguration.class)
class MedicationManagerImplTest {
    @Autowired
    private MedicationManagerImpl manager;
    @MockBean
    private UserManager userManager;
    @MockBean
    private MedicationEntityRepository medicationEntityRepository;
    @MockBean
    private MedicationTypeEntityRepository medicationTypeEntityRepository;
    @MockBean
    private AdministrationTypeEntityRepository administrationTypeEntityRepository;
    @MockBean
    private DoseTypeEntityRepository doseTypeEntityRepository;
    @Captor
    private ArgumentCaptor<MedicationEntity> anyEntity;

    @Nested
    class createForCurrentUser {
        @Test
        void returnsDTO() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medicationType = Instancio.create(MedicationTypeEntity.class);
            var doseType = Instancio.create(DoseTypeEntity.class);
            var administrationType = Instancio.create(AdministrationTypeEntity.class);
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .set(field(CreateMedicationRequestDTO::medicationTypeId), medicationType.getId())
                .set(field(CreateMedicationRequestDTO::administrationTypeId), administrationType.getId())
                .set(field(CreateMedicationRequestDTO::doseTypeId), doseType.getId())
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationTypeEntityRepository.findById(any())).thenReturn(Optional.of(medicationType));
            when(doseTypeEntityRepository.findByIdAndMedicationTypeId(any(), any())).thenReturn(Optional.of(doseType));
            when(administrationTypeEntityRepository.findByIdAndMedicationTypeId(any(), any())).thenReturn(Optional.of(administrationType));
            when(medicationEntityRepository.save(any())).thenAnswer(returnsFirstArg());
            // Then
            MedicationDTO result = manager.createForCurrentUser(request);
            verify(userManager).findCurrentUser();
            verify(medicationTypeEntityRepository).findById(request.medicationTypeId());
            verify(doseTypeEntityRepository).findByIdAndMedicationTypeId(request.doseTypeId(), request.medicationTypeId());
            verify(administrationTypeEntityRepository).findByIdAndMedicationTypeId(request.administrationTypeId(), request.medicationTypeId());
            verify(medicationEntityRepository).save(anyEntity.capture());
            assertThat(result).isEqualTo(new MedicationDTO(
                anyEntity.getValue().getId(),
                request.name(),
                new MedicationTypeDTO(medicationType.getId(), medicationType.getName()),
                new AdministrationTypeDTO(administrationType.getId(), administrationType.getName()),
                new DoseTypeDTO(doseType.getId(), doseType.getName()),
                request.dosesPerPackage(),
                request.color()
            ));
        }

        @Test
        void savesEntity() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medicationType = Instancio.create(MedicationTypeEntity.class);
            var doseType = Instancio.create(DoseTypeEntity.class);
            var administrationType = Instancio.create(AdministrationTypeEntity.class);
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .set(field(CreateMedicationRequestDTO::medicationTypeId), medicationType.getId())
                .set(field(CreateMedicationRequestDTO::administrationTypeId), administrationType.getId())
                .set(field(CreateMedicationRequestDTO::doseTypeId), doseType.getId())
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationTypeEntityRepository.findById(any())).thenReturn(Optional.of(medicationType));
            when(doseTypeEntityRepository.findByIdAndMedicationTypeId(any(), any())).thenReturn(Optional.of(doseType));
            when(administrationTypeEntityRepository.findByIdAndMedicationTypeId(any(), any())).thenReturn(Optional.of(administrationType));
            when(medicationEntityRepository.save(any())).thenAnswer(returnsFirstArg());
            // Then
            manager.createForCurrentUser(request);
            verify(userManager).findCurrentUser();
            verify(medicationTypeEntityRepository).findById(request.medicationTypeId());
            verify(doseTypeEntityRepository).findByIdAndMedicationTypeId(request.doseTypeId(), request.medicationTypeId());
            verify(administrationTypeEntityRepository).findByIdAndMedicationTypeId(request.administrationTypeId(), request.medicationTypeId());
            verify(medicationEntityRepository).save(anyEntity.capture());
            assertThat(anyEntity.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(new MedicationEntity(
                    user.id(),
                    request.name(),
                    medicationType,
                    administrationType,
                    doseType,
                    request.dosesPerPackage(),
                    request.color()
                ));
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var request = Instancio.create(CreateMedicationRequestDTO.class);
            // Then
            assertThatExceptionOfType(InvalidMedicationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("User is not authenticated");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfMedicationTypeNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var request = Instancio.create(CreateMedicationRequestDTO.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(MedicationTypeNotFoundException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Medication type with ID '" + request.medicationTypeId() + "' does not exist");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfDoseTypeNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medicationType = Instancio.create(MedicationTypeEntity.class);
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .set(field(CreateMedicationRequestDTO::medicationTypeId), medicationType.getId())
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationTypeEntityRepository.findById(any())).thenReturn(Optional.of(medicationType));
            // Then
            assertThatExceptionOfType(DoseTypeNotFoundException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Dose type with ID '" + request.doseTypeId() + "' does not exist");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfAdministrationTypeNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var medicationType = Instancio.create(MedicationTypeEntity.class);
            var doseType = Instancio.create(DoseTypeEntity.class);
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .set(field(CreateMedicationRequestDTO::medicationTypeId), medicationType.getId())
                .set(field(CreateMedicationRequestDTO::doseTypeId), doseType.getId())
                .create();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationTypeEntityRepository.findById(any())).thenReturn(Optional.of(medicationType));
            when(doseTypeEntityRepository.findByIdAndMedicationTypeId(any(), any())).thenReturn(Optional.of(doseType));
            // Then
            assertThatExceptionOfType(AdministrationTypeNotFoundException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessage("Administration type with ID '" + request.administrationTypeId() + "' does not exist");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfNameNotGiven() {
            // Given
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .ignore(field(CreateMedicationRequestDTO::name))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Name is required");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfNameTooLong() {
            // Given
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .generate(field(CreateMedicationRequestDTO::name), gen -> gen.string().length(129))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Name cannot contain more than 128 characters");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfMedicationTypeNotGiven() {
            // Given
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .ignore(field(CreateMedicationRequestDTO::medicationTypeId))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Medication type is required");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfAdministrationTYpeNotGiven() {
            // Given
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .ignore(field(CreateMedicationRequestDTO::administrationTypeId))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Administration type is required");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfDoseTypeNotGiven() {
            // Given
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .ignore(field(CreateMedicationRequestDTO::doseTypeId))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("Dose type is required");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfDosesPerPackageNotGiven() {
            // Given
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .ignore(field(CreateMedicationRequestDTO::dosesPerPackage))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("The amount of doses per package is required");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfDosesPerPackageIsNegative() {
            // Given
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .generate(field(CreateMedicationRequestDTO::dosesPerPackage), gen -> gen.doubles().max(-1d).as(BigDecimal::valueOf))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("The amount of doses per package must be zero or positive");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfColorNotGiven() {
            // Given
            var request = Instancio.of(CreateMedicationRequestDTO.class)
                .ignore(field(CreateMedicationRequestDTO::color))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request))
                .withMessageEndingWith("The color is required");
            verifyNoInteractions(medicationEntityRepository);
        }
    }

    @Nested
    class findAllForCurrentUser {
        @ParameterizedTest
        @CsvSource(value = {
            "null",
            "''",
            "'  '"
        }, nullValues = "null")
        void returnsDTO_whenNotUsingSearch(String search) {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(MedicationEntity.class)
                .set(field(MedicationEntity::getUserId), user.id())
                .create();
            var request = PageRequest.of(0, 20);
            // When
            when(medicationEntityRepository.findAllByUserId(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            Page<MedicationDTO> results = manager.findAllForCurrentUser(search, request);
            assertThat(results.getContent()).containsOnly(new MedicationDTO(
                entity.getId(),
                entity.getName(),
                new MedicationTypeDTO(entity.getMedicationType().getId(), entity.getMedicationType().getName()),
                new AdministrationTypeDTO(entity.getAdministrationType().getId(), entity.getAdministrationType().getName()),
                new DoseTypeDTO(entity.getDoseType().getId(), entity.getDoseType().getName()),
                entity.getDosesPerPackage(),
                entity.getColor()
            ));
            verify(medicationEntityRepository).findAllByUserId(user.id(), request);
            verify(userManager).findCurrentUser();
        }

        @Test
        void returnsDTO_whenUsingSearch() {
            // Given
            var searchTerm = "search";
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(MedicationEntity.class)
                .set(field(MedicationEntity::getUserId), user.id())
                .create();
            var request = PageRequest.of(0, 20);
            // When
            when(medicationEntityRepository.findAllByUserIdAndNameContainingIgnoreCase(any(), any(), any())).thenReturn(new PageImpl<>(List.of(entity)));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            Page<MedicationDTO> results = manager.findAllForCurrentUser(searchTerm, request);
            assertThat(results.getContent()).containsOnly(new MedicationDTO(
                entity.getId(),
                entity.getName(),
                new MedicationTypeDTO(entity.getMedicationType().getId(), entity.getMedicationType().getName()),
                new AdministrationTypeDTO(entity.getAdministrationType().getId(), entity.getAdministrationType().getName()),
                new DoseTypeDTO(entity.getDoseType().getId(), entity.getDoseType().getName()),
                entity.getDosesPerPackage(),
                entity.getColor()
            ));
            verify(medicationEntityRepository).findAllByUserIdAndNameContainingIgnoreCase(user.id(), searchTerm, request);
            verify(userManager).findCurrentUser();
        }

        @Test
        void throwsExceptionIfUserNotFound() {
            // Given
            var request = PageRequest.of(0, 20);
            // Then
            assertThatExceptionOfType(InvalidMedicationException.class)
                .isThrownBy(() -> manager.findAllForCurrentUser(null, request))
                .withMessage("User is not authenticated");
            verifyNoInteractions(medicationEntityRepository);
        }
    }

    @Nested
    class findByIdForCurrentUser {
        @Test
        void returnsDTO() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(MedicationEntity.class)
                .set(field(MedicationEntity::getUserId), user.id())
                .create();
            // When
            when(medicationEntityRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            Optional<MedicationDTO> result = manager.findByIdForCurrentUser(entity.getId());
            assertThat(result).contains(new MedicationDTO(
                entity.getId(),
                entity.getName(),
                new MedicationTypeDTO(entity.getMedicationType().getId(), entity.getMedicationType().getName()),
                new AdministrationTypeDTO(entity.getAdministrationType().getId(), entity.getAdministrationType().getName()),
                new DoseTypeDTO(entity.getDoseType().getId(), entity.getDoseType().getName()),
                entity.getDosesPerPackage(),
                entity.getColor()
            ));
            verify(userManager).findCurrentUser();
            verify(medicationEntityRepository).findByIdAndUserId(entity.getId(), user.id());
        }

        @Test
        void throwsExceptionNotAuthenticated() {
            // Given
            var id = UUID.randomUUID();
            // Then
            assertThatExceptionOfType(InvalidMedicationException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(id))
                .withMessage("User is not authenticated");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void returnsNothingIfNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var id = UUID.randomUUID();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            Optional<MedicationDTO> result = manager.findByIdForCurrentUser(id);
            assertThat(result).isEmpty();
            verify(userManager).findCurrentUser();
            verify(medicationEntityRepository).findByIdAndUserId(id, user.id());
        }
    }

    @Nested
    class findByIdAndUserId {
        @Test
        void returnsDTO() {
            // Given
            var entity = Instancio.create(MedicationEntity.class);
            // When
            when(medicationEntityRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            // Then
            Optional<MedicationDTO> result = manager.findByIdAndUserId(entity.getId(), entity.getUserId());
            assertThat(result).contains(new MedicationDTO(
                entity.getId(),
                entity.getName(),
                new MedicationTypeDTO(entity.getMedicationType().getId(), entity.getMedicationType().getName()),
                new AdministrationTypeDTO(entity.getAdministrationType().getId(), entity.getAdministrationType().getName()),
                new DoseTypeDTO(entity.getDoseType().getId(), entity.getDoseType().getName()),
                entity.getDosesPerPackage(),
                entity.getColor()
            ));
            verify(medicationEntityRepository).findByIdAndUserId(entity.getId(), entity.getUserId());
        }

        @Test
        void returnsNothingIfNotFound() {
            // Given
            var id = UUID.randomUUID();
            var userId = UUID.randomUUID();
            // Then
            Optional<MedicationDTO> result = manager.findByIdAndUserId(id, userId);
            assertThat(result).isEmpty();
            verify(medicationEntityRepository).findByIdAndUserId(id, userId);
        }
    }

    @Nested
    class deleteByIdForCurrentUser {
        @Test
        void deletes() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var entity = Instancio.of(MedicationEntity.class)
                .set(field(MedicationEntity::getUserId), user.id())
                .create();
            // When
            when(medicationEntityRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            manager.deleteByIdForCurrentUser(entity.getId());
            verify(userManager).findCurrentUser();
            verify(medicationEntityRepository).findByIdAndUserId(entity.getId(), user.id());
            verify(medicationEntityRepository).delete(entity);
        }

        @Test
        void throwsExceptionNotAuthenticated() {
            // Given
            var id = UUID.randomUUID();
            // Then
            assertThatExceptionOfType(InvalidMedicationException.class)
                .isThrownBy(() -> manager.deleteByIdForCurrentUser(id))
                .withMessage("User is not authenticated");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var id = UUID.randomUUID();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(MedicationNotFoundException.class)
                .isThrownBy(() -> manager.deleteByIdForCurrentUser(id));
        }
    }

    @Nested
    class updateForCurrentUser {
        @Test
        void returnsDTO() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var doseType = Instancio.create(DoseTypeEntity.class);
            var administrationType = Instancio.create(AdministrationTypeEntity.class);
            var request = Instancio.of(UpdateMedicationRequestDTO.class)
                .set(field(UpdateMedicationRequestDTO::administrationTypeId), administrationType.getId())
                .set(field(UpdateMedicationRequestDTO::doseTypeId), doseType.getId())
                .create();
            var entity = Instancio.create(MedicationEntity.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(doseTypeEntityRepository.findByIdAndMedicationTypeId(any(), any())).thenReturn(Optional.of(doseType));
            when(administrationTypeEntityRepository.findByIdAndMedicationTypeId(any(), any())).thenReturn(Optional.of(administrationType));
            when(medicationEntityRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            // Then
            MedicationDTO result = manager.updateForCurrentUser(entity.getId(), request);
            verify(userManager).findCurrentUser();
            verify(medicationEntityRepository).findByIdAndUserId(entity.getId(), user.id());
            verify(doseTypeEntityRepository).findByIdAndMedicationTypeId(request.doseTypeId(), entity.getMedicationType().getId());
            verify(administrationTypeEntityRepository).findByIdAndMedicationTypeId(request.administrationTypeId(), entity.getMedicationType().getId());
            assertThat(result).isEqualTo(new MedicationDTO(
                entity.getId(),
                request.name(),
                new MedicationTypeDTO(entity.getMedicationType().getId(), entity.getMedicationType().getName()),
                new AdministrationTypeDTO(administrationType.getId(), administrationType.getName()),
                new DoseTypeDTO(doseType.getId(), doseType.getName()),
                request.dosesPerPackage(),
                request.color()
            ));
        }

        @Test
        void savesEntity() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var doseType = Instancio.create(DoseTypeEntity.class);
            var administrationType = Instancio.create(AdministrationTypeEntity.class);
            var request = Instancio.of(UpdateMedicationRequestDTO.class)
                .set(field(UpdateMedicationRequestDTO::administrationTypeId), administrationType.getId())
                .set(field(UpdateMedicationRequestDTO::doseTypeId), doseType.getId())
                .create();
            var entity = Instancio.create(MedicationEntity.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(doseTypeEntityRepository.findByIdAndMedicationTypeId(any(), any())).thenReturn(Optional.of(doseType));
            when(administrationTypeEntityRepository.findByIdAndMedicationTypeId(any(), any())).thenReturn(Optional.of(administrationType));
            when(medicationEntityRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            // Then
            manager.updateForCurrentUser(entity.getId(), request);
            verify(userManager).findCurrentUser();
            verify(doseTypeEntityRepository).findByIdAndMedicationTypeId(request.doseTypeId(), entity.getMedicationType().getId());
            verify(administrationTypeEntityRepository).findByIdAndMedicationTypeId(request.administrationTypeId(), entity.getMedicationType().getId());
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new MedicationEntity(
                    entity.getId(),
                    entity.getUserId(),
                    request.name(),
                    entity.getMedicationType(),
                    administrationType,
                    doseType,
                    request.dosesPerPackage(),
                    request.color()
                ));
        }

        @Test
        void throwsExceptionIfNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var request = Instancio.create(UpdateMedicationRequestDTO.class);
            var id = UUID.randomUUID();
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(MedicationNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Medication with ID '" + id + "' does not exist");
        }

        @Test
        void throwsExceptionIfUserNotAuthenticated() {
            // Given
            var request = Instancio.create(UpdateMedicationRequestDTO.class);
            var id = UUID.randomUUID();
            // Then
            assertThatExceptionOfType(InvalidMedicationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("User is not authenticated");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfDoseTypeNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var id = UUID.randomUUID();
            var request = Instancio.create(UpdateMedicationRequestDTO.class);
            var entity = Instancio.create(MedicationEntity.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationEntityRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            // Then
            assertThatExceptionOfType(DoseTypeNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Dose type with ID '" + request.doseTypeId() + "' does not exist");
        }

        @Test
        void throwsExceptionIfAdministrationTypeNotFound() {
            // Given
            var user = Instancio.create(UserDTO.class);
            var id = UUID.randomUUID();
            var doseType = Instancio.create(DoseTypeEntity.class);
            var request = Instancio.of(UpdateMedicationRequestDTO.class)
                .set(field(UpdateMedicationRequestDTO::doseTypeId), doseType.getId())
                .create();
            var entity = Instancio.create(MedicationEntity.class);
            // When
            when(userManager.findCurrentUser()).thenReturn(Optional.of(user));
            when(medicationEntityRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(entity));
            when(doseTypeEntityRepository.findByIdAndMedicationTypeId(any(), any())).thenReturn(Optional.of(doseType));
            // Then
            assertThatExceptionOfType(AdministrationTypeNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Administration type with ID '" + request.administrationTypeId() + "' does not exist");
        }

        @Test
        void throwsExceptionIfNameNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateMedicationRequestDTO.class)
                .ignore(field(UpdateMedicationRequestDTO::name))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Name is required");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfNameTooLong() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateMedicationRequestDTO.class)
                .generate(field(UpdateMedicationRequestDTO::name), gen -> gen.string().length(129))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Name cannot contain more than 128 characters");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfAdministrationTYpeNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateMedicationRequestDTO.class)
                .ignore(field(UpdateMedicationRequestDTO::administrationTypeId))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Administration type is required");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfDoseTypeNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateMedicationRequestDTO.class)
                .ignore(field(UpdateMedicationRequestDTO::doseTypeId))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("Dose type is required");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfDosesPerPackageNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateMedicationRequestDTO.class)
                .ignore(field(UpdateMedicationRequestDTO::dosesPerPackage))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("The amount of doses per package is required");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfDosesPerPackageIsNegative() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateMedicationRequestDTO.class)
                .generate(field(UpdateMedicationRequestDTO::dosesPerPackage), gen -> gen.doubles().max(-1d).as(BigDecimal::valueOf))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("The amount of doses per package must be zero or positive");
            verifyNoInteractions(medicationEntityRepository);
        }

        @Test
        void throwsExceptionIfColorNotGiven() {
            // Given
            var id = UUID.randomUUID();
            var request = Instancio.of(UpdateMedicationRequestDTO.class)
                .ignore(field(UpdateMedicationRequestDTO::color))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageEndingWith("The color is required");
            verifyNoInteractions(medicationEntityRepository);
        }
    }
}