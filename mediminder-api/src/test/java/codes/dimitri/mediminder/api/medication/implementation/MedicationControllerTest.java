package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.medication.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicationController.class)
@Import(SecurityConfiguration.class)
class MedicationControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private MedicationManager manager;

    @Nested
    class findAll {
        @Test
        void returnsResults() throws Exception {
            var medication = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var page = new PageImpl<>(List.of(medication));
            var pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "name");
            var search = "daf";
            when(manager.findAllForCurrentUser(search, pageRequest)).thenReturn(page);
            mvc
                .perform(get("/api/medication")
                    .param("search", search)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "name,asc")
                    .with(user("me1@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(medication.id().toString()));
        }
    }

    @Nested
    class findById {
        @Test
        void returnsResult() throws Exception {
            var medication = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            when(manager.findByIdForCurrentUser(medication.id())).thenReturn(medication);
            mvc
                .perform(get("/api/medication/{id}", medication.id())
                    .with(user("me1@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(medication.id().toString()));
        }

        @Test
        void returnsNotFound() throws Exception {
            var medicationId = UUID.randomUUID();
            var exception = new MedicationNotFoundException(medicationId);
            when(manager.findByIdForCurrentUser(medicationId)).thenThrow(exception);
            mvc
                .perform(get("/api/medication/{id}", medicationId)
                    .with(user("me@example.org")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Medication not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/medication/notfound"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class create {
        @Test
        void returnsResult() throws Exception {
            var medication = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var request = new CreateMedicationRequestDTO(
                "Dafalgan",
                "TABLET",
                "ORAL",
                "TABLET",
                new BigDecimal("100"),
                Color.RED
            );
            var json = """
                {
                    "name": "Dafalgan",
                    "medicationTypeId": "TABLET",
                    "administrationTypeId": "ORAL",
                    "doseTypeId": "TABLET",
                    "dosesPerPackage": 100,
                    "color": "RED"
                }
                """;
            when(manager.createForCurrentUser(request)).thenReturn(medication);
            mvc
                .perform(post("/api/medication")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(medication.id().toString()));
        }

        @Test
        void returnsInvalid() throws Exception {
            var request = new CreateMedicationRequestDTO(
                "Dafalgan",
                "TABLET",
                "ORAL",
                "TABLET",
                new BigDecimal("100"),
                Color.RED
            );
            var json = """
                {
                    "name": "Dafalgan",
                    "medicationTypeId": "TABLET",
                    "administrationTypeId": "ORAL",
                    "doseTypeId": "TABLET",
                    "dosesPerPackage": 100,
                    "color": "RED"
                }
                """;
            var exception = new InvalidMedicationException("Invalid medication");
            when(manager.createForCurrentUser(request)).thenThrow(exception);
            mvc
                .perform(post("/api/medication")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid medication"))
                .andExpect(jsonPath("$.type").value("https://mediminder/medication/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var request = new CreateMedicationRequestDTO(
                "Dafalgan",
                "TABLET",
                "ORAL",
                "TABLET",
                new BigDecimal("100"),
                Color.RED
            );
            var json = """
                {
                    "name": "Dafalgan",
                    "medicationTypeId": "TABLET",
                    "administrationTypeId": "ORAL",
                    "doseTypeId": "TABLET",
                    "dosesPerPackage": 100,
                    "color": "RED"
                }
                """;
            var exception = new ConstraintViolationException("Validation failed", null);
            when(manager.createForCurrentUser(request)).thenThrow(exception);
            mvc
                .perform(post("/api/medication")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid medication"))
                .andExpect(jsonPath("$.type").value("https://mediminder/medication/invalid"))
                .andExpect(jsonPath("$.detail").value("Validation failed"));
        }
    }

    @Nested
    class update {
        @Test
        void returnsResult() throws Exception {
            var medication = new MedicationDTO(
                UUID.randomUUID(),
                "Dafalgan",
                new MedicationTypeDTO("TABLET", "Tablet"),
                new AdministrationTypeDTO("ORAL", "Oral"),
                new DoseTypeDTO("TABLET", "tablet(s)"),
                new BigDecimal("100"),
                Color.RED
            );
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan",
                "ORAL",
                "TABLET",
                new BigDecimal("100"),
                Color.RED
            );
            var json = """
                {
                    "name": "Dafalgan",
                    "administrationTypeId": "ORAL",
                    "doseTypeId": "TABLET",
                    "dosesPerPackage": 100,
                    "color": "RED"
                }
                """;
            when(manager.updateForCurrentUser(medication.id(), request)).thenReturn(medication);
            mvc
                .perform(put("/api/medication/{id}", medication.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(medication.id().toString()));
        }

        @Test
        void returnsNotFound() throws Exception {
            var medicationId = UUID.randomUUID();
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan",
                "ORAL",
                "TABLET",
                new BigDecimal("100"),
                Color.RED
            );
            var json = """
                {
                    "name": "Dafalgan",
                    "administrationTypeId": "ORAL",
                    "doseTypeId": "TABLET",
                    "dosesPerPackage": 100,
                    "color": "RED"
                }
                """;
            var exception = new MedicationNotFoundException(medicationId);
            when(manager.updateForCurrentUser(medicationId, request)).thenThrow(exception);
            mvc
                .perform(put("/api/medication/{id}", medicationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Medication not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/medication/notfound"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsInvalid() throws Exception {
            var medicationId = UUID.randomUUID();
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan",
                "ORAL",
                "TABLET",
                new BigDecimal("100"),
                Color.RED
            );
            var json = """
                {
                    "name": "Dafalgan",
                    "administrationTypeId": "ORAL",
                    "doseTypeId": "TABLET",
                    "dosesPerPackage": 100,
                    "color": "RED"
                }
                """;
            var exception = new InvalidMedicationException("Invalid medication");
            when(manager.updateForCurrentUser(medicationId, request)).thenThrow(exception);
            mvc
                .perform(put("/api/medication/{id}", medicationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid medication"))
                .andExpect(jsonPath("$.type").value("https://mediminder/medication/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var medicationId = UUID.randomUUID();
            var request = new UpdateMedicationRequestDTO(
                "Dafalgan",
                "ORAL",
                "TABLET",
                new BigDecimal("100"),
                Color.RED
            );
            var json = """
                {
                    "name": "Dafalgan",
                    "administrationTypeId": "ORAL",
                    "doseTypeId": "TABLET",
                    "dosesPerPackage": 100,
                    "color": "RED"
                }
                """;
            var exception = new ConstraintViolationException("Validation failed", null);
            when(manager.updateForCurrentUser(medicationId, request)).thenThrow(exception);
            mvc
                .perform(put("/api/medication/{id}", medicationId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid medication"))
                .andExpect(jsonPath("$.type").value("https://mediminder/medication/invalid"))
                .andExpect(jsonPath("$.detail").value("Validation failed"));
        }
    }

    @Nested
    class delete {
        @Test
        void returnsNoContent() throws Exception {
            var medicationId = UUID.randomUUID();
            mvc
                .perform(delete("/api/medication/{id}", medicationId)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).deleteByIdForCurrentUser(medicationId);
        }

        @Test
        void returnsNotFound() throws Exception {
            var medicationId = UUID.randomUUID();
            var exception = new MedicationNotFoundException(medicationId);
            doThrow(exception).when(manager).deleteByIdForCurrentUser(medicationId);
            mvc
                .perform(delete("/api/medication/{id}", medicationId)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Medication not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/medication/notfound"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }
}