package codes.dimitri.mediminder.api.document.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.document.*;
import codes.dimitri.mediminder.api.medication.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@Import(SecurityConfiguration.class)
class DocumentControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private DocumentManager manager;

    @Nested
    class findAll {
        @Test
        void returnsResults() throws Exception {
            var pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
            LocalDate expiryDate = LocalDate.of(2026, 1, 31);
            var document = new DocumentDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "filename.pdf",
                expiryDate,
                new MedicationDTO(
                    UUID.randomUUID(),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                "Package insert Dafalgan"
            );
            when(manager.findAllForCurrentUser(expiryDate, pageRequest)).thenReturn(new PageImpl<>(List.of(document)));
            mvc
                .perform(get("/api/document")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc")
                    .param("expiredOn", "2026-01-31")
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content[0].id").value(document.id().toString()));
        }
    }

    @Nested
    class findById {
        @Test
        void returnsResult() throws Exception {
            var document = new DocumentDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "filename.pdf",
                LocalDate.of(2026, 1, 31),
                new MedicationDTO(
                    UUID.randomUUID(),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                "Package insert Dafalgan"
            );
            when(manager.findByIdForCurrentUser(document.id())).thenReturn(document);
            mvc
                .perform(get("/api/document/{id}", document.id())
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(document.id().toString()));
        }

        @Test
        void returnsNotFound() throws Exception {
            var documentId = UUID.randomUUID();
            var exception = new DocumentNotFoundException(documentId);
            when(manager.findByIdForCurrentUser(documentId)).thenThrow(exception);
            mvc
                .perform(get("/api/document/{id}", documentId)
                    .with(user("me@example.org")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail").value(exception.getMessage()))
                .andExpect(jsonPath("title").value("Document not found"))
                .andExpect(jsonPath("type").value("https://mediminder/document/notfound"));
        }
    }

    @Nested
    class create {
        @Test
        void returnsResult() throws Exception {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2026, 1, 31),
                UUID.fromString("a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e"),
                "Package insert Dafalgan"
            );
            var result = new DocumentDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "filename.pdf",
                LocalDate.of(2026, 1, 31),
                new MedicationDTO(
                    UUID.fromString("a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e"),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                "Package insert Dafalgan"
            );
            var json = """
                {
                    "relatedMedicationId": "a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e",
                    "expiresAt": "2026-01-31",
                    "description": "Package insert Dafalgan"
                }
                """;
            var file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "content".getBytes());
            when(manager.createForCurrentUser(request, file)).thenReturn(result);
            mvc
                .perform(multipart("/api/document")
                    .file(file)
                    .file(jsonPart("request", json))
                    .contentType("multipart/form-data")
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(result.id().toString()));
        }

        @Test
        void returnsInvalid() throws Exception {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2026, 1, 31),
                UUID.fromString("a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e"),
                "Package insert Dafalgan"
            );
            var json = """
                {
                    "relatedMedicationId": "a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e",
                    "expiresAt": "2026-01-31",
                    "description": "Package insert Dafalgan"
                }
                """;
            var file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "content".getBytes());
            var exception = new InvalidDocumentException("Invalid document");
            when(manager.createForCurrentUser(request, file)).thenThrow(exception);
            mvc
                .perform(multipart("/api/document")
                    .file(file)
                    .file(jsonPart("request", json))
                    .contentType("multipart/form-data")
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("detail").value(exception.getMessage()))
                .andExpect(jsonPath("title").value("Invalid document"))
                .andExpect(jsonPath("type").value("https://mediminder/document/invalid"));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var request = new CreateDocumentRequestDTO(
                LocalDate.of(2026, 1, 31),
                UUID.fromString("a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e"),
                "Package insert Dafalgan"
            );
            var json = """
                {
                    "relatedMedicationId": "a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e",
                    "expiresAt": "2026-01-31",
                    "description": "Package insert Dafalgan"
                }
                """;
            var file = new MockMultipartFile("file", "filename.pdf", "application/pdf", "content".getBytes());
            var exception = new ConstraintViolationException("Validation failed", null);
            when(manager.createForCurrentUser(request, file)).thenThrow(exception);
            mvc
                .perform(multipart("/api/document")
                    .file(file)
                    .file(jsonPart("request", json))
                    .contentType("multipart/form-data")
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("detail").value(exception.getMessage()))
                .andExpect(jsonPath("title").value("Invalid document"))
                .andExpect(jsonPath("type").value("https://mediminder/document/invalid"));
        }
    }

    @Nested
    class update {
        @Test
        void returnsResult() throws Exception {
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2026, 1, 31),
                UUID.fromString("a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e"),
                "Package insert Dafalgan"
            );
            var document = new DocumentDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "filename.pdf",
                LocalDate.of(2026, 1, 31),
                new MedicationDTO(
                    UUID.fromString("a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e"),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                "Package insert Dafalgan"
            );
            var json = """
                {
                    "relatedMedicationId": "a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e",
                    "expiresAt": "2026-01-31",
                    "description": "Package insert Dafalgan"
                }
                """;
            when(manager.updateForCurrentUser(document.id(), request)).thenReturn(document);
            mvc
                .perform(put("/api/document/{id}", document.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(document.id().toString()));
        }

        @Test
        void returnsNotFound() throws Exception {
            var documentId = UUID.randomUUID();
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2026, 1, 31),
                UUID.fromString("a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e"),
                "Package insert Dafalgan"
            );
            var exception = new DocumentNotFoundException(documentId);
            when(manager.updateForCurrentUser(documentId, request)).thenThrow(exception);
            mvc
                .perform(put("/api/document/{id}", documentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "relatedMedicationId": "a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e",
                            "expiresAt": "2026-01-31",
                            "description": "Package insert Dafalgan"
                        }
                        """)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail").value(exception.getMessage()))
                .andExpect(jsonPath("title").value("Document not found"))
                .andExpect(jsonPath("type").value("https://mediminder/document/notfound"));
        }

        @Test
        void returnsInvalid() throws Exception {
            var documentId = UUID.randomUUID();
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2026, 1, 31),
                UUID.fromString("a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e"),
                "Package insert Dafalgan"
            );
            var exception = new InvalidDocumentException("Invalid document");
            when(manager.updateForCurrentUser(documentId, request)).thenThrow(exception);
            mvc
                .perform(put("/api/document/{id}", documentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "relatedMedicationId": "a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e",
                            "expiresAt": "2026-01-31",
                            "description": "Package insert Dafalgan"
                        }
                        """)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("detail").value(exception.getMessage()))
                .andExpect(jsonPath("title").value("Invalid document"))
                .andExpect(jsonPath("type").value("https://mediminder/document/invalid"));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var documentId = UUID.randomUUID();
            var request = new UpdateDocumentRequestDTO(
                LocalDate.of(2026, 1, 31),
                UUID.fromString("a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e"),
                "Package insert Dafalgan"
            );
            var exception = new ConstraintViolationException("Validation failed", null);
            when(manager.updateForCurrentUser(documentId, request)).thenThrow(exception);
            mvc
                .perform(put("/api/document/{id}", documentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "relatedMedicationId": "a0eebc4b-1f2d-4b8c-9f3d-5a7e6f8b1c2e",
                            "expiresAt": "2026-01-31",
                            "description": "Package insert Dafalgan"
                        }
                        """)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("detail").value(exception.getMessage()))
                .andExpect(jsonPath("title").value("Invalid document"))
                .andExpect(jsonPath("type").value("https://mediminder/document/invalid"));
        }
    }

    @Nested
    class download {
        @Test
        void returnsResult() throws Exception {
            var resource = new ResourceWithMetadataDTO(
                new ByteArrayResource("content".getBytes()),
                "file.pdf",
                "application/pdf"
            );
            var id = UUID.randomUUID();
            when(manager.downloadDocumentForCurrentUser(id)).thenReturn(resource);
            mvc
                .perform(get("/api/document/{id}/download", id)
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"file.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes("content".getBytes()));
        }

        @Test
        void notFound() throws Exception {
            var id = UUID.randomUUID();
            var exception = new DocumentNotFoundException(id);
            when(manager.downloadDocumentForCurrentUser(id)).thenThrow(exception);
            mvc
                .perform(get("/api/document/{id}/download", id)
                    .with(user("me@example.org")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail").value(exception.getMessage()))
                .andExpect(jsonPath("title").value("Document not found"))
                .andExpect(jsonPath("type").value("https://mediminder/document/notfound"));
        }
    }

    @Nested
    class delete {
        @Test
        void returnsNoContent() throws Exception {
            var id = UUID.randomUUID();
            mvc
                .perform(delete("/api/document/{id}", id)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).deleteForCurrentUser(id);
        }

        @Test
        void notFound() throws Exception {
            var id = UUID.randomUUID();
            var exception = new DocumentNotFoundException(id);
            doThrow(exception).when(manager).deleteForCurrentUser(id);
            mvc
                .perform(delete("/api/document/{id}", id)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound());
        }
    }

    private @NotNull MockMultipartFile jsonPart(String name, String json) throws JsonProcessingException {
        return new MockMultipartFile(name, null, "application/json", json.getBytes());
    }
}