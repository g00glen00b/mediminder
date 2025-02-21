package codes.dimitri.mediminder.api.medication.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.medication.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MedicationController.class)
@Import(SecurityConfiguration.class)
class MedicationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MedicationManager manager;

    @Test
    @WithMockUser
    void findAll() throws Exception {
        // Given
        var medication = Instancio.create(MedicationDTO.class);
        // When
        when(manager.findAllForCurrentUser(anyString(), any())).thenReturn(new PageImpl<>(List.of(medication)));
        // Then
        mockMvc
            .perform(get("/api/medication")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "name,asc")
                .queryParam("search", "test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(medication.id().toString()));
        verify(manager).findAllForCurrentUser("test", PageRequest.of(0, 10, Sort.Direction.ASC, "name"));
    }

    @Test
    void findAll_unuuthorized() throws Exception {
        mockMvc
            .perform(get("/api/medication")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "name,asc")
                .queryParam("search", "test"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void findById() throws Exception {
        // Given
        var medication = Instancio.create(MedicationDTO.class);
        // When
        when(manager.findByIdForCurrentUser(any())).thenReturn(Optional.of(medication));
        // Then
        mockMvc
            .perform(get("/api/medication/{id}", medication.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(medication.id().toString()));
        verify(manager).findByIdForCurrentUser(medication.id());
    }

    @Test
    void findById_unauthorized() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(get("/api/medication/{id}", id))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void findById_notFound() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(get("/api/medication/{id}", id))
            .andExpect(status().isNotFound());
        verify(manager).findByIdForCurrentUser(id);
    }

    @Test
    @WithMockUser
    void create() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "medicationTypeId": "TABLET",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        var medication = Instancio.create(MedicationDTO.class);
        // When
        when(manager.createForCurrentUser(any())).thenReturn(medication);
        // Then
        mockMvc
            .perform(post("/api/medication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(medication.id().toString()));
        verify(manager).createForCurrentUser(new CreateMedicationRequestDTO(
            "Dafalgan 1g",
            "TABLET",
            "ORAL",
            "TABLET",
            new BigDecimal("100"),
            Color.RED
        ));
    }

    @Test
    @WithMockUser
    void create_withoutCsrf() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "medicationTypeId": "TABLET",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        // Then
        mockMvc
            .perform(post("/api/medication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    void create_unauthorized() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "medicationTypeId": "TABLET",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        // Then
        mockMvc
            .perform(post("/api/medication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void create_constraintViolation() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "medicationTypeId": "TABLET",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        var violation = new MessageConstraintViolation("Name is required");
        // When
        when(manager.createForCurrentUser(any())).thenThrow(new ConstraintViolationException(Set.of(violation)));
        // Then
        mockMvc
            .perform(post("/api/medication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Name is required"));
    }

    @Test
    @WithMockUser
    void create_invalidMedicationException() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "medicationTypeId": "TABLET",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        // When
        when(manager.createForCurrentUser(any())).thenThrow(new InvalidMedicationException("User is not authenticated"));
        // Then
        mockMvc
            .perform(post("/api/medication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User is not authenticated"));
    }

    @Test
    @WithMockUser
    void create_medicationTypeNotFound() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "medicationTypeId": "DOESNOTEXIST",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        // When
        when(manager.createForCurrentUser(any())).thenThrow(new MedicationTypeNotFoundException("DOESNOTEXIST"));
        // Then
        mockMvc
            .perform(post("/api/medication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Medication type with ID 'DOESNOTEXIST' does not exist"));
    }

    @Test
    @WithMockUser
    void create_doseTypeNotFound() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "medicationTypeId": "TABLET",
                "administrationTypeId": "ORAL",
                "doseTypeId": "DOESNOTEXIST",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        // When
        when(manager.createForCurrentUser(any())).thenThrow(new DoseTypeNotFoundException("DOESNOTEXIST"));
        // Then
        mockMvc
            .perform(post("/api/medication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Dose type with ID 'DOESNOTEXIST' does not exist"));
    }

    @Test
    @WithMockUser
    void create_administrationTypeNotFound() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "medicationTypeId": "TABLET",
                "administrationTypeId": "DOESNOTEXIST",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        // When
        when(manager.createForCurrentUser(any())).thenThrow(new AdministrationTypeNotFoundException("DOESNOTEXIST"));
        // Then
        mockMvc
            .perform(post("/api/medication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Administration type with ID 'DOESNOTEXIST' does not exist"));
    }

    @Test
    @WithMockUser
    void update() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        var id = UUID.randomUUID();
        var medication = Instancio.create(MedicationDTO.class);
        // When
        when(manager.updateForCurrentUser(any(), any())).thenReturn(medication);
        // Then
        mockMvc
            .perform(put("/api/medication/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(medication.id().toString()));
        verify(manager).updateForCurrentUser(id, new UpdateMedicationRequestDTO(
            "Dafalgan 1g",
            "ORAL",
            "TABLET",
            new BigDecimal("100"),
            Color.RED
        ));
    }

    @Test
    @WithMockUser
    void update_withoutCsrf() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(put("/api/medication/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    void update_unauthorized() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(put("/api/medication/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void update_constraintViolation() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        var id = UUID.randomUUID();
        var violation = new MessageConstraintViolation("Name is required");
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(new ConstraintViolationException(Set.of(violation)));
        // Then
        mockMvc
            .perform(put("/api/medication/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Name is required"));
    }

    @Test
    @WithMockUser
    void update_invalidMedicationException() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        var id = UUID.randomUUID();
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(new InvalidMedicationException("User is not authenticated"));
        // Then
        mockMvc
            .perform(put("/api/medication/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User is not authenticated"));
    }

    @Test
    @WithMockUser
    void update_doseTypeNotFound() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "administrationTypeId": "ORAL",
                "doseTypeId": "DOESNOTEXIST",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        var id = UUID.randomUUID();
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(new DoseTypeNotFoundException("DOESNOTEXIST"));
        // Then
        mockMvc
            .perform(put("/api/medication/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Dose type with ID 'DOESNOTEXIST' does not exist"));
    }

    @Test
    @WithMockUser
    void update_administrationTypeNotFound() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "administrationTypeId": "DOESNOTEXIST",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        var id = UUID.randomUUID();
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(new AdministrationTypeNotFoundException("DOESNOTEXIST"));
        // Then
        mockMvc
            .perform(put("/api/medication/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Administration type with ID 'DOESNOTEXIST' does not exist"));
    }

    @Test
    @WithMockUser
    void update_medicationNotFound() throws Exception {
        // Given
        var request = """
            {
                "name": "Dafalgan 1g",
                "administrationTypeId": "ORAL",
                "doseTypeId": "TABLET",
                "dosesPerPackage": 100,
                "color": "RED"
            }
        """;
        var id = UUID.randomUUID();
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(new MedicationNotFoundException(id));
        // Then
        mockMvc
            .perform(put("/api/medication/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Medication with ID '" + id + "' does not exist"));
    }

    @Test
    @WithMockUser
    void delete_ok() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/medication/{id}", id)
                .with(csrf()))
            .andExpect(status().isOk());
        verify(manager).deleteByIdForCurrentUser(id);
    }

    @Test
    void delete_unauthorized() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/medication/{id}", id)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void delete_withoutCsrf() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/medication/{id}", id))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void delete_notFound() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // When
        doThrow(new MedicationNotFoundException(id)).when(manager).deleteByIdForCurrentUser(any());
        // Then
        mockMvc
            .perform(delete("/api/medication/{id}", id)
                .with(csrf()))
            .andExpect(status().isNotFound());
        verify(manager).deleteByIdForCurrentUser(id);
    }

    private record MessageConstraintViolation(String message) implements ConstraintViolation<Object> {

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getMessageTemplate() {
            return null;
        }

        @Override
        public Object getRootBean() {
            return null;
        }

        @Override
        public Class<Object> getRootBeanClass() {
            return null;
        }

        @Override
        public Object getLeafBean() {
            return null;
        }

        @Override
        public Object[] getExecutableParameters() {
            return new Object[0];
        }

        @Override
        public Object getExecutableReturnValue() {
            return null;
        }

        @Override
        public Path getPropertyPath() {
            return null;
        }

        @Override
        public Object getInvalidValue() {
            return null;
        }

        @Override
        public ConstraintDescriptor<?> getConstraintDescriptor() {
            return null;
        }

        @Override
        public <U> U unwrap(Class<U> aClass) {
            return null;
        }
    }
}