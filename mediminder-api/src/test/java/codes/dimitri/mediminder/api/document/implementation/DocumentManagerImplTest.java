package codes.dimitri.mediminder.api.document.implementation;

import codes.dimitri.mediminder.api.document.*;
import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Comparator.reverseOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ApplicationModuleTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2",
    "document.storage-location=./target/test-storage"
})
@Transactional
@Sql("classpath:test-data/documents.sql")
@Sql(value = "classpath:test-data/cleanup-documents.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DocumentManagerImplTest {
    @Autowired
    private DocumentManager manager;
    @Autowired
    private DocumentEntityRepository repository;
    @Autowired
    private DocumentStorageService storageService;
    @MockitoBean
    private UserManager userManager;
    @MockitoBean
    private MedicationManager medicationManager;

    @AfterAll
    static void afterAll() throws IOException {
        deleteStorageDirectory();
    }

    @Nested
    class createForCurrentUser {
        @Test
        void returnsResult() {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            var file = new MockMultipartFile("file", "file.pdf", "application/pdf", "content".getBytes());
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                request.relatedMedicationId(),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(request.relatedMedicationId(), user.id())).thenReturn(medication);
            var result = manager.createForCurrentUser(request, file);
            assertThat(result).isEqualTo(new DocumentDTO(
                result.id(),
                user.id(),
                "file.pdf",
                LocalDate.of(2025, 6, 30),
                medication,
                "Package insert Dafalgan"
            ));
        }

        @Test
        void savesEntity() {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            var file = new MockMultipartFile("file", "file.pdf", "application/pdf", "content".getBytes());
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                request.relatedMedicationId(),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(request.relatedMedicationId(), user.id())).thenReturn(medication);
            var result = manager.createForCurrentUser(request, file);
            DocumentEntity entity = repository.findById(result.id()).orElseThrow();
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new DocumentEntity(
                result.id(),
                user.id(),
                "file.pdf",
                "application/pdf",
                LocalDate.of(2025, 6, 30),
                request.relatedMedicationId(),
                "Package insert Dafalgan"
            ));
        }

        @Test
        void failsIfMedicationNotFound() {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            var file = new MockMultipartFile("file", "file.pdf", "application/pdf", "content".getBytes());
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(request.relatedMedicationId(), user.id())).thenThrow(new MedicationNotFoundException(request.relatedMedicationId()));
            assertThatExceptionOfType(InvalidDocumentException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request, file))
                .withMessage("Medication not found");
        }

        @Test
        void doesNotRetrieveMedicationIfNoMedicationGiven() {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                null,
                "Package insert Dafalgan"
            );
            var file = new MockMultipartFile("file", "file.pdf", "application/pdf", "content".getBytes());
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            manager.createForCurrentUser(request, file);
            verifyNoInteractions(medicationManager);
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            var file = new MockMultipartFile("file", "file.pdf", "application/pdf", "content".getBytes());
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                request.relatedMedicationId(),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidDocumentException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request, file))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfRequestNotGiven() {
            var file = new MockMultipartFile("file", "file.pdf", "application/pdf", "content".getBytes());
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(null, file));
        }

        @Test
        void failsIfFileNotGiven() {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request, null));
        }

        @Test
        void failsIfDescriptionTooLong() {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "a".repeat(129)
            );
            var file = new MockMultipartFile("file", "file.pdf", "application/pdf", "content".getBytes());
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request, file))
                .withMessageContaining("Description should not contain more than 128 characters");
        }

        @Test
        void failsIfFileNotSupportedContentType() {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            var file = new MockMultipartFile("file", "file.exe", "application/unknown", "content".getBytes());
            assertThatExceptionOfType(InvalidDocumentException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request, file))
                .withMessage("Files with content type 'application/unknown' are not allowed");
        }

        @Test
        void failsIfFilenameTooLong() {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            var file = new MockMultipartFile("file", "a".repeat(129) + ".pdf", "application/pdf", "content".getBytes());
            assertThatExceptionOfType(InvalidDocumentException.class)
                .isThrownBy(() -> manager.createForCurrentUser(request, file))
                .withMessage("Filename '" + "a".repeat(129) + ".pdf' cannot contain more than 128 characters");
        }
    }

    @Nested
    class updateForCurrentUser {
        @Test
        void returnsResult() {
            var id = UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb");
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                request.relatedMedicationId(),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(request.relatedMedicationId(), user.id())).thenReturn(medication);
            var result = manager.updateForCurrentUser(id, request);
            assertThat(result).isEqualTo(new DocumentDTO(
                result.id(),
                user.id(),
                "file.pdf",
                LocalDate.of(2025, 6, 30),
                medication,
                "Package insert Dafalgan"
            ));
        }

        @Test
        void savesEntity() {
            var id = UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb");
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                request.relatedMedicationId(),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(request.relatedMedicationId(), user.id())).thenReturn(medication);
            manager.updateForCurrentUser(id, request);
            DocumentEntity entity = repository.findById(id).orElseThrow();
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new DocumentEntity(
                    id,
                    user.id(),
                    "file.pdf",
                    "application/pdf",
                    LocalDate.of(2025, 6, 30),
                    request.relatedMedicationId(),
                    "Package insert Dafalgan"
                ));
        }

        @Test
        void failsIfMedicationDoesNotExist() {
            var id = UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb");
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(request.relatedMedicationId(), user.id())).thenThrow(new MedicationNotFoundException(request.relatedMedicationId()));
            assertThatExceptionOfType(InvalidDocumentException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Medication not found");
        }

        @Test
        void doesNotFailIfMedicationNotGiven() {
            var id = UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb");
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                null,
                "Package insert Dafalgan"
            );
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            manager.updateForCurrentUser(id, request);
            verifyNoInteractions(medicationManager);
        }

        @Test
        void failsIfEntityNotFound() {
            var id = UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb");
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            var user = new UserDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(DocumentNotFoundException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessage("Document with ID '" + id + "' does not exist");
        }

        @Test
        void failsIfIdNotGiven() {
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "Package insert Dafalgan"
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(null, request));
        }

        @Test
        void failsIfRequestNotGiven() {
            var id = UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb");
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, null));
        }

        @Test
        void failsIfDescriptionTooLong() {
            var id = UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb");
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2025, 6, 30),
                UUID.randomUUID(),
                "a".repeat(129)
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateForCurrentUser(id, request))
                .withMessageContaining("Description should not contain more than 128 characters");
        }
    }

    @Nested
    class findAllForCurrentUser {
        @Test
        void returnsResults() {
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var pageRequest = PageRequest.of(0, 10);
            var medication = new MedicationDTO(
                UUID.fromString("dcb33d3c-5e8e-4f54-b965-64dc17e0a285"),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medication.id(), user.id())).thenReturn(medication);
            var result = manager.findAllForCurrentUser(null, null, pageRequest);
            assertThat(result).containsOnly(new DocumentDTO(
                UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb"),
                user.id(),
                "file.pdf",
                LocalDate.of(2026, 1, 31),
                medication,
                "Medical attest for Hydrocortisone"
            ));
        }

        @Test
        void returnsResultsWithMedicationId() {
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var pageRequest = PageRequest.of(0, 10);
            var medication = new MedicationDTO(
                UUID.fromString("dcb33d3c-5e8e-4f54-b965-64dc17e0a285"),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medication.id(), user.id())).thenReturn(medication);
            var result = manager.findAllForCurrentUser(null, medication.id(), pageRequest);
            assertThat(result).containsOnly(new DocumentDTO(
                UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb"),
                user.id(),
                "file.pdf",
                LocalDate.of(2026, 1, 31),
                medication,
                "Medical attest for Hydrocortisone"
            ));
        }

        @ParameterizedTest
        @CsvSource({
            "2026-01-30,0",
            "2026-01-31,1",
        })
        void returnsResultsWithExpiryDate(LocalDate expiresOn, int expectedResults) {
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenReturn(user);
            var result = manager.findAllForCurrentUser(expiresOn, null, pageRequest);
            assertThat(result).hasSize(expectedResults);
        }

        @Test
        void failsIfPageableNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllForCurrentUser(null, null, null));
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var pageRequest = PageRequest.of(0, 10);
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidDocumentException.class)
                .isThrownBy(() -> manager.findAllForCurrentUser(null, null, pageRequest))
                .withMessage("User is not authenticated");
        }

        @Test
        void returnsResultEvenIfMedicationNotFound() {
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var pageRequest = PageRequest.of(0, 10);
            UUID medicationId = UUID.fromString("dcb33d3c-5e8e-4f54-b965-64dc17e0a285");
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medicationId, user.id())).thenThrow(new MedicationNotFoundException(medicationId));
            var result = manager.findAllForCurrentUser(null, null, pageRequest);
            assertThat(result).containsOnly(new DocumentDTO(
                UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb"),
                user.id(),
                "file.pdf",
                LocalDate.of(2026, 1, 31),
                null,
                "Medical attest for Hydrocortisone"
            ));
        }
    }

    @Nested
    class findByIdForCurrentUser {
        @Test
        void returnsResult() {
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            var medication = new MedicationDTO(
                UUID.fromString("dcb33d3c-5e8e-4f54-b965-64dc17e0a285"),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medication.id(), user.id())).thenReturn(medication);
            var result = manager.findByIdForCurrentUser(UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb"));
            assertThat(result).isEqualTo(new DocumentDTO(
                result.id(),
                user.id(),
                "file.pdf",
                LocalDate.of(2026, 1, 31),
                medication,
                "Medical attest for Hydrocortisone"
            ));
        }

        @Test
        void returnsResultWithoutMedication() {
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            UUID medicationId = UUID.fromString("dcb33d3c-5e8e-4f54-b965-64dc17e0a285");
            when(userManager.findCurrentUser()).thenReturn(user);
            when(medicationManager.findByIdAndUserId(medicationId, user.id())).thenThrow(new MedicationNotFoundException(medicationId));
            var result = manager.findByIdForCurrentUser(UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb"));
            assertThat(result).isEqualTo(new DocumentDTO(
                result.id(),
                user.id(),
                "file.pdf",
                LocalDate.of(2026, 1, 31),
                null,
                "Medical attest for Hydrocortisone"
            ));
        }

        @Test
        void failsIfEntityNotFound() {
            var user = new UserDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(DocumentNotFoundException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb")))
                .withMessage("Document with ID 'af3edd34-a8e2-4356-9877-2481eae06dfb' does not exist");
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findByIdForCurrentUser(null));
        }
    }

    @Nested
    class downloadDocumentForCurrentUser {
        @Test
        void returnsFile() throws IOException {
            UUID id = UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb");
            DocumentEntity entity = repository.findById(id).orElseThrow();
            var file = new MockMultipartFile("file", entity.getFilename(), entity.getContentType(), "content".getBytes());
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            storageService.upload(entity, file);
            ResourceWithMetadataDTO result = manager.downloadDocumentForCurrentUser(id);
            assertThat(result.resource().getContentAsByteArray()).isEqualTo("content".getBytes());
            assertThat(result.filename()).isEqualTo("file.pdf");
            assertThat(result.contentType()).isEqualTo("application/pdf");
        }

        @Test
        void failsIfEntityNotFound() {
            var user = new UserDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(DocumentNotFoundException.class)
                .isThrownBy(() -> manager.downloadDocumentForCurrentUser(UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb")))
                .withMessage("Document with ID 'af3edd34-a8e2-4356-9877-2481eae06dfb' does not exist");
        }

        @Test
        void failsIfUserNotAuthenticated() {
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidDocumentException.class)
                .isThrownBy(() -> manager.downloadDocumentForCurrentUser(UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb")))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.downloadDocumentForCurrentUser(null));
        }
    }

    @Nested
    class deleteForCurrentUser {
        @Test
        void deletesEntity() {
            var user = new UserDTO(
                UUID.fromString("c00385bb-3551-4cd9-b786-8a71f1c1b9d8"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            UUID id = UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb");
            when(userManager.findCurrentUser()).thenReturn(user);
            manager.deleteForCurrentUser(id);
            assertThat(repository.existsById(id)).isFalse();
        }

        @Test
        void failsIfEntityNotFound() {
            var user = new UserDTO(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                true,
                false
            );
            when(userManager.findCurrentUser()).thenReturn(user);
            assertThatExceptionOfType(DocumentNotFoundException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb")))
                .withMessage("Document with ID 'af3edd34-a8e2-4356-9877-2481eae06dfb' does not exist");
        }

        @Test
        void failsIfUserNotAuthenticated() {
            when(userManager.findCurrentUser()).thenThrow(new CurrentUserNotFoundException());
            assertThatExceptionOfType(InvalidDocumentException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(UUID.fromString("af3edd34-a8e2-4356-9877-2481eae06dfb")))
                .withMessage("User is not authenticated");
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.deleteForCurrentUser(null));
        }
    }

    @Nested
    class findAllWithExpiryDateBefore {
        @Test
        void returnsResults() {
            var pageRequest = PageRequest.of(0, 10);
            var result = manager.findAllWithExpiryDateBefore(LocalDate.of(2026, 1, 31), pageRequest);
            assertThat(result).hasSize(1);
        }

        @Test
        void failsIfPageableNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllWithExpiryDateBefore(LocalDate.now(), null));
        }

        @Test
        void failsIfExpiryDateNotGiven() {
            var pageRequest = PageRequest.of(0, 10);
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findAllWithExpiryDateBefore(null, pageRequest));
        }
    }

    private static void deleteStorageDirectory() throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get("./target/test-storage"))) {
            walk
                .sorted(reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }
}