package codes.dimitri.mediminder.api.cabinet.implementation;

import codes.dimitri.mediminder.api.cabinet.*;
import codes.dimitri.mediminder.api.common.SecurityConfiguration;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CabinetEntryController.class)
@Import(SecurityConfiguration.class)
class CabinetEntryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CabinetEntryManager manager;

    @Test
    @WithMockUser
    void findAll() throws Exception {
        // Given
        var dto = Instancio.create(CabinetEntryDTO.class);
        var page = new PageImpl<>(List.of(dto));
        // When
        when(manager.findAllForCurrentUser(any())).thenReturn(page);
        // Then
        mockMvc
            .perform(get("/api/cabinet")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "expiryDate,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(dto.id().toString()));
        verify(manager).findAllForCurrentUser(PageRequest.of(0, 10, Sort.Direction.ASC, "expiryDate"));
    }

    @Test
    void findAll_unauthorized() throws Exception {
        mockMvc
            .perform(get("/api/cabinet")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "expiryDate,asc"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void findById() throws Exception {
        // Given
        var dto = Instancio.create(CabinetEntryDTO.class);
        // When
        when(manager.findByIdForCurrentUser(any())).thenReturn(dto);
        // Then
        mockMvc
            .perform(get("/api/cabinet/{id}", dto.id()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dto.id().toString()));
        verify(manager).findByIdForCurrentUser(dto.id());
    }

    @Test
    void findById_unauthorized() throws Exception {
        // Given
        var dto = Instancio.create(CabinetEntryDTO.class);
        // Then
        mockMvc
            .perform(get("/api/cabinet/{id}", dto.id()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void findById_notFound() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // When
        when(manager.findByIdForCurrentUser(any())).thenThrow(new CabinetEntryNotFoundException(id));
        // Then
        mockMvc
            .perform(get("/api/cabinet/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type").value("https://mediminder/cabinet/notfound"))
            .andExpect(jsonPath("$.title").value("Cabinet entry not found"))
            .andExpect(jsonPath("$.detail").value("Cabinet entry with ID '" + id + "' does not exist"));
        verify(manager).findByIdForCurrentUser(id);
    }

    @Test
    @WithMockUser
    void create() throws Exception {
        // Given
        var dto = Instancio.create(CabinetEntryDTO.class);
        var request = """
            {
                "medicationId": "972da877-3638-3e3b-a1c1-4c7f10428a65",
                "remainingDoses": 10,
                "expiryDate": "2024-06-30"
            }
            """;
        // When
        when(manager.createForCurrentUser(any())).thenReturn(dto);
        // Then
        mockMvc
            .perform(post("/api/cabinet")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dto.id().toString()));
        verify(manager).createForCurrentUser(new CreateCabinetEntryRequestDTO(
            UUID.fromString("972da877-3638-3e3b-a1c1-4c7f10428a65"),
            new BigDecimal("10"),
            LocalDate.of(2024, 6, 30)
        ));
    }

    @Test
    void create_unauthorized() throws Exception {
        // Given
        var request = """
            {
                "medicationId": "972da877-3638-3e3b-a1c1-4c7f10428a65",
                "remainingDoses": 10,
                "expiryDate": "2024-06-30"
            }
            """;
        // Then
        mockMvc
            .perform(post("/api/cabinet")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void create_invalid() throws Exception {
        // Given
        var request = """
            {
                "medicationId": "972da877-3638-3e3b-a1c1-4c7f10428a65",
                "remainingDoses": 10,
                "expiryDate": "2024-06-30"
            }
            """;
        // When
        when(manager.createForCurrentUser(any())).thenThrow(new InvalidCabinetEntryException("Medication does not exist"));
        // Then
        mockMvc
            .perform(post("/api/cabinet")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Medication does not exist"));
    }

    @Test
    @WithMockUser
    void create_constraintViolation() throws Exception {
        // Given
        var violation = new MessageConstraintViolation("Medication is required");
        var request = """
            {
                "medicationId": "972da877-3638-3e3b-a1c1-4c7f10428a65",
                "remainingDoses": 10,
                "expiryDate": "2024-06-30"
            }
            """;
        // When
        when(manager.createForCurrentUser(any())).thenThrow(new ConstraintViolationException(Set.of(violation)));
        // Then
        mockMvc
            .perform(post("/api/cabinet")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Medication is required"));
    }

    @Test
    @WithMockUser
    void update() throws Exception {
        // Given
        var dto = Instancio.create(CabinetEntryDTO.class);
        var request = """
            {
                "remainingDoses": 10,
                "expiryDate": "2024-06-30"
            }
            """;
        // When
        when(manager.updateForCurrentUser(any(), any())).thenReturn(dto);
        // Then
        mockMvc
            .perform(put("/api/cabinet/{id}", dto.id())
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dto.id().toString()));
        verify(manager).updateForCurrentUser(dto.id(), new UpdateCabinetEntryRequestDTO(
            new BigDecimal("10"),
            LocalDate.of(2024, 6, 30)
        ));
    }

    @Test
    void update_unauthorized() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var request = """
            {
                "remainingDoses": 10,
                "expiryDate": "2024-06-30"
            }
            """;
        // Then
        mockMvc
            .perform(put("/api/cabinet/{id}", id)
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void update_invalid() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var request = """
            {
                "remainingDoses": 10,
                "expiryDate": "2024-06-30"
            }
            """;
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(new InvalidCabinetEntryException("Remaining doses is not valid"));
        // Then
        mockMvc
            .perform(put("/api/cabinet/{id}", id)
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Remaining doses is not valid"));
    }

    @Test
    @WithMockUser
    void update_notFound() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var request = """
            {
                "remainingDoses": 10,
                "expiryDate": "2024-06-30"
            }
            """;
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(new CabinetEntryNotFoundException(id));
        // Then
        mockMvc
            .perform(put("/api/cabinet/{id}", id)
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Cabinet entry with ID '" + id + "' does not exist"));
    }

    @Test
    @WithMockUser
    void update_constraintViolation() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var violation = new MessageConstraintViolation("Remaining doses is required");
        var request = """
            {
                "remainingDoses": 10,
                "expiryDate": "2024-06-30"
            }
            """;
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(new ConstraintViolationException(Set.of(violation)));
        // Then
        mockMvc
            .perform(put("/api/cabinet/{id}", id)
                .content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Remaining doses is required"));
    }

    @Test
    @WithMockUser
    void delete_ok() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/cabinet/{id}", id)
                .with(csrf()))
            .andExpect(status().isOk());
        verify(manager).deleteForCurrentUser(id);
    }

    @Test
    void delete_unauthorized() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/cabinet/{id}", id)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void delete_notFound() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // When
        doThrow(new CabinetEntryNotFoundException(id)).when(manager).deleteForCurrentUser(any());
        // Then
        mockMvc
            .perform(delete("/api/cabinet/{id}", id)
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Cabinet entry with ID '" + id + "' does not exist"));
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