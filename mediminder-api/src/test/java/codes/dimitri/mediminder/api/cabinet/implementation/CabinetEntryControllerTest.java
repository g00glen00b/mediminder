package codes.dimitri.mediminder.api.cabinet.implementation;

import codes.dimitri.mediminder.api.cabinet.*;
import codes.dimitri.mediminder.api.medication.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CabinetEntryController.class)
class CabinetEntryControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CabinetEntryManager manager;

    @Nested
    class findAll {
        @Test
        void returnsResults() throws Exception {
            var entry = new CabinetEntryDTO(
                UUID.randomUUID(),
                "auth|ff9d85fcc3c505949092c",
                new MedicationDTO(
                    UUID.randomUUID(),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                new BigDecimal("20"),
                LocalDate.of(2025, 6, 30)
            );
            var page = new PageImpl<>(List.of(entry));
            var pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
            when(manager.findAllForCurrentUser(null, pageRequest)).thenReturn(page);
            mvc
                .perform(get("/api/cabinet")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc")
                    .with(user("me1@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(entry.id().toString()));
        }
    }

    @Nested
    class findById {
        @Test
        void returnsResult() throws Exception {
            var entry = new CabinetEntryDTO(
                UUID.randomUUID(),
                "auth|ff9d85fcc3c505949092c",
                new MedicationDTO(
                    UUID.randomUUID(),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                new BigDecimal("20"),
                LocalDate.of(2025, 6, 30)
            );
            when(manager.findByIdForCurrentUser(entry.id())).thenReturn(entry);
            mvc
                .perform(get("/api/cabinet/{id}", entry.id())
                    .with(user("me1@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entry.id().toString()));
        }

        @Test
        void returnsNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            var exception = new CabinetEntryNotFoundException(id);
            when(manager.findByIdForCurrentUser(id)).thenThrow(exception);
            mvc
                .perform(get("/api/cabinet/{id}", id)
                    .with(user("me1@example.org")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.title").value("Cabinet entry not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/cabinet/notfound"));
        }
    }

    @Nested
    class create {
        @Test
        void returnsResult() throws Exception {
            var entry = new CabinetEntryDTO(
                UUID.randomUUID(),
                "auth|ff9d85fcc3c505949092c",
                new MedicationDTO(
                    UUID.fromString("20cf421d-d78c-4e64-8bc4-6c614ae74053"),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                new BigDecimal("20"),
                LocalDate.of(2025, 6, 30)
            );
            var request = new CreateCabinetEntryRequestDTO(
                entry.medication().id(),
                entry.remainingDoses(),
                entry.expiryDate()
            );
            var json = """
            {
                "medicationId": "20cf421d-d78c-4e64-8bc4-6c614ae74053",
                "remainingDoses": 20,
                "expiryDate": "2025-06-30"
            }
            """;
            when(manager.createForCurrentUser(request)).thenReturn(entry);
            mvc
                .perform(post("/api/cabinet")
                    .contentType("application/json")
                    .content(json)
                    .with(user("me1@example.org"))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(entry.id().toString()));
        }

        @Test
        void returnsInvalid() throws Exception {
            var request = new CreateCabinetEntryRequestDTO(
                UUID.fromString("20cf421d-d78c-4e64-8bc4-6c614ae74053"),
                new BigDecimal("20"),
                LocalDate.of(2025, 6, 30)
            );
            var json = """
            {
                "medicationId": "20cf421d-d78c-4e64-8bc4-6c614ae74053",
                "remainingDoses": 20,
                "expiryDate": "2025-06-30"
            }
            """;
            var exception = new InvalidCabinetEntryException("Medication not found");
            when(manager.createForCurrentUser(request)).thenThrow(exception);
            mvc
                .perform(post("/api/cabinet")
                    .contentType("application/json")
                    .content(json)
                    .with(user("me1@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.title").value("Invalid cabinet entry"))
                .andExpect(jsonPath("$.type").value("https://mediminder/cabinet/invalid"));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var request = new CreateCabinetEntryRequestDTO(
                UUID.fromString("20cf421d-d78c-4e64-8bc4-6c614ae74053"),
                new BigDecimal("20"),
                LocalDate.of(2025, 6, 30)
            );
            var json = """
            {
                "medicationId": "20cf421d-d78c-4e64-8bc4-6c614ae74053",
                "remainingDoses": 20,
                "expiryDate": "2025-06-30"
            }
            """;
            var exception = new ConstraintViolationException("Validation failed", null);
            when(manager.createForCurrentUser(request)).thenThrow(exception);
            mvc
                .perform(post("/api/cabinet")
                    .contentType("application/json")
                    .content(json)
                    .with(user("me1@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.title").value("Invalid cabinet entry"))
                .andExpect(jsonPath("$.type").value("https://mediminder/cabinet/invalid"));
        }
    }

    @Nested
    class update {
        @Test
        void returnsResult() throws Exception {
            var entry = new CabinetEntryDTO(
                UUID.randomUUID(),
                "auth|ff9d85fcc3c505949092c",
                new MedicationDTO(
                    UUID.fromString("20cf421d-d78c-4e64-8bc4-6c614ae74053"),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                new BigDecimal("20"),
                LocalDate.of(2025, 6, 30)
            );
            var request = new UpdateCabinetEntryRequestDTO(
                entry.remainingDoses(),
                entry.expiryDate()
            );
            var json = """
            {
                "remainingDoses": 20,
                "expiryDate": "2025-06-30"
            }
            """;
            when(manager.updateForCurrentUser(entry.id(), request)).thenReturn(entry);
            mvc
                .perform(put("/api/cabinet/{id}", entry.id())
                    .contentType("application/json")
                    .content(json)
                    .with(user("me1@example.org"))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entry.id().toString()));
        }

        @Test
        void returnsNotFound() throws Exception {
            var id = UUID.randomUUID();
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("20"),
                LocalDate.of(2025, 6, 30)
            );
            var json = """
            {
                "remainingDoses": 20,
                "expiryDate": "2025-06-30"
            }
            """;
            var exception = new CabinetEntryNotFoundException(id);
            when(manager.updateForCurrentUser(id, request)).thenThrow(exception);
            mvc
                .perform(put("/api/cabinet/{id}", id)
                    .contentType("application/json")
                    .content(json)
                    .with(user("me1@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.title").value("Cabinet entry not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/cabinet/notfound"));
        }

        @Test
        void returnsInvalid() throws Exception {
            var id = UUID.randomUUID();
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("20"),
                LocalDate.of(2025, 6, 30)
            );
            var json = """
            {
                "remainingDoses": 20,
                "expiryDate": "2025-06-30"
            }
            """;
            var exception = new InvalidCabinetEntryException("Remaining doses too high");
            when(manager.updateForCurrentUser(id, request)).thenThrow(exception);
            mvc
                .perform(put("/api/cabinet/{id}", id)
                    .contentType("application/json")
                    .content(json)
                    .with(user("me1@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.title").value("Invalid cabinet entry"))
                .andExpect(jsonPath("$.type").value("https://mediminder/cabinet/invalid"));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var id = UUID.randomUUID();
            var request = new UpdateCabinetEntryRequestDTO(
                new BigDecimal("20"),
                LocalDate.of(2025, 6, 30)
            );
            var json = """
            {
                "remainingDoses": 20,
                "expiryDate": "2025-06-30"
            }
            """;
            var exception = new ConstraintViolationException("Validation failed", null);
            when(manager.updateForCurrentUser(id, request)).thenThrow(exception);
            mvc
                .perform(put("/api/cabinet/{id}", id)
                    .contentType("application/json")
                    .content(json)
                    .with(user("me1@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.title").value("Invalid cabinet entry"))
                .andExpect(jsonPath("$.type").value("https://mediminder/cabinet/invalid"));
        }
    }

    @Nested
    class delete {
        @Test
        void returnsNoContent() throws Exception {
            var id = UUID.randomUUID();
            mvc
                .perform(delete("/api/cabinet/{id}", id)
                    .with(user("me1@example.org"))
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).deleteForCurrentUser(id);
        }

        @Test
        void handleNotFound() throws Exception {
            var id = UUID.randomUUID();
            var exception = new CabinetEntryNotFoundException(id);
            doThrow(exception).when(manager).deleteForCurrentUser(id);
            mvc
                .perform(delete("/api/cabinet/{id}", id)
                    .with(user("me1@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(exception.getMessage()))
                .andExpect(jsonPath("$.title").value("Cabinet entry not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/cabinet/notfound"));
        }
    }
}