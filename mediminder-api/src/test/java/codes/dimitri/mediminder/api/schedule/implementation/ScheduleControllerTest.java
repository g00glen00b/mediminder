package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.schedule.*;
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
import java.time.LocalTime;
import java.time.Period;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ScheduleController.class)
@Import(SecurityConfiguration.class)
class ScheduleControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ScheduleManager manager;

    @Test
    @WithMockUser
    void findAll_ok() throws Exception {
        // Given
        var schedule = Instancio.create(ScheduleDTO.class);
        // When
        when(manager.findAllForCurrentUser(any())).thenReturn(new PageImpl<>(List.of(schedule)));
        // Then
        mockMvc
            .perform(get("/api/schedule")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "medicationId,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(schedule.id().toString()));
        verify(manager).findAllForCurrentUser(PageRequest.of(0, 10, Sort.Direction.ASC, "medicationId"));
    }

    @Test
    @WithMockUser
    void findAll_invalidSchedule() throws Exception {
        // Given
        var exception = new InvalidScheduleException("User is not authenticated");
        // When
        when(manager.findAllForCurrentUser(any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(get("/api/schedule")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "medicationId,asc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User is not authenticated"));
    }

    @Test
    void findAll_unauthorized() throws Exception {
        // Then
        mockMvc
            .perform(get("/api/schedule")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "medicationId,asc"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void create_ok() throws Exception {
        // Given
        var request = """
        {
            "medicationId": "972da877-3638-3e3b-a1c1-4c7f10428a65",
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        var schedule = Instancio.create(ScheduleDTO.class);
        // When
        when(manager.createForCurrentUser(any())).thenReturn(schedule);
        // Then
        mockMvc
            .perform(post("/api/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(schedule.id().toString()));
        verify(manager).createForCurrentUser(new CreateScheduleRequestDTO(
            UUID.fromString("972da877-3638-3e3b-a1c1-4c7f10428a65"),
            Period.ofDays(1),
            new SchedulePeriodDTO(LocalDate.of(2024, 6, 30), null),
            LocalTime.of(10, 0),
            "Before breakfast",
            new BigDecimal(1)
        ));
    }

    @Test
    @WithMockUser
    void create_invalidSchedule() throws Exception {
        // Given
        var request = """
        {
            "medicationId": "972da877-3638-3e3b-a1c1-4c7f10428a65",
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        var exception = new InvalidScheduleException("User not authenticated");
        // When
        when(manager.createForCurrentUser(any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(post("/api/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User not authenticated"));
    }

    @Test
    void create_unauthorized() throws Exception {
        // Given
        var request = """
        {
            "medicationId": "972da877-3638-3e3b-a1c1-4c7f10428a65",
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        // Then
        mockMvc
            .perform(post("/api/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void create_withoutCsrf() throws Exception {
        // Given
        var request = """
        {
            "medicationId": "972da877-3638-3e3b-a1c1-4c7f10428a65",
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        // Then
        mockMvc
            .perform(post("/api/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void create_constraintViolation() throws Exception {
        // Given
        var request = """
        {
            "medicationId": "972da877-3638-3e3b-a1c1-4c7f10428a65",
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        var violation = new MessageConstraintViolation("Medication ID is not given");
        var exception = new ConstraintViolationException(Set.of(violation));
        // When
        when(manager.createForCurrentUser(any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(post("/api/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Medication ID is not given"));
    }

    @Test
    @WithMockUser
    void update_ok() throws Exception {
        // Given
        var request = """
        {
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        var schedule = Instancio.create(ScheduleDTO.class);
        // When
        when(manager.updateForCurrentUser(any(), any())).thenReturn(schedule);
        // Then
        mockMvc
            .perform(put("/api/schedule/{id}", schedule.id())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(schedule.id().toString()));
        verify(manager).updateForCurrentUser(schedule.id(), new UpdateScheduleRequestDTO(
            Period.ofDays(1),
            new SchedulePeriodDTO(LocalDate.of(2024, 6, 30), null),
            LocalTime.of(10, 0),
            "Before breakfast",
            new BigDecimal(1)
        ));
    }

    @Test
    @WithMockUser
    void update_invalidSchedule() throws Exception {
        // Given
        var request = """
        {
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        var exception = new InvalidScheduleException("User not authenticated");
        var id = UUID.randomUUID();
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(put("/api/schedule/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User not authenticated"));
    }

    @Test
    @WithMockUser
    void update_notFound() throws Exception {
        // Given
        var request = """
        {
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        var id = UUID.randomUUID();
        var exception = new ScheduleNotFoundException(id);
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(put("/api/schedule/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Schedule with ID '" + id + "' does not exist"));
    }

    @Test
    void update_unauthorized() throws Exception {
        // Given
        var request = """
        {
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(put("/api/schedule/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void update_withoutCsrf() throws Exception {
        // Given
        var request = """
        {
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(put("/api/schedule/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void update_constraintViolation() throws Exception {
        // Given
        var request = """
        {
            "interval": "P1D",
            "period": {
                "startingAt": "2024-06-30",
                "endingAtInclusive": null
            },
            "time": "10:00",
            "dose": 1,
            "description": "Before breakfast"
        }
        """;
        var id = UUID.randomUUID();
        var violation = new MessageConstraintViolation("Medication ID is not given");
        var exception = new ConstraintViolationException(Set.of(violation));
        // When
        when(manager.updateForCurrentUser(any(), any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(put("/api/schedule/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Medication ID is not given"));
    }

    @Test
    @WithMockUser
    void delete_ok() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/schedule/{id}", id)
                .with(csrf()))
            .andExpect(status().isOk());
        verify(manager).deleteForCurrentUser(id);
    }

    @Test
    @WithMockUser
    void delete_invalidSchedule() throws Exception {
        // Given
        var exception = new InvalidScheduleException("User not authenticated");
        var id = UUID.randomUUID();
        // When
        doThrow(exception).when(manager).deleteForCurrentUser(any());
        // Then
        mockMvc
            .perform(delete("/api/schedule/{id}", id)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User not authenticated"));
    }

    @Test
    @WithMockUser
    void delete_notFound() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var exception = new ScheduleNotFoundException(id);
        // When
        doThrow(exception).when(manager).deleteForCurrentUser(any());
        // Then
        mockMvc
            .perform(delete("/api/schedule/{id}", id)
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Schedule with ID '" + id + "' does not exist"));
    }

    @Test
    void delete_unauthorized() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(put("/api/schedule/{id}", id)
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
            .perform(delete("/api/schedule/{id}", id))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void findById_ok() throws Exception {
        // Given
        var schedule = Instancio.create(ScheduleDTO.class);
        // When
        when(manager.findByIdForCurrentUser(any())).thenReturn(schedule);
        // Then
        mockMvc
            .perform(get("/api/schedule/{id}", schedule.id())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(schedule.id().toString()));
        verify(manager).findByIdForCurrentUser(schedule.id());
    }

    @Test
    @WithMockUser
    void findById_invalidSchedule() throws Exception {
        // Given
        var exception = new InvalidScheduleException("User not authenticated");
        var id = UUID.randomUUID();
        // When
        when(manager.findByIdForCurrentUser(any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(get("/api/schedule/{id}", id)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User not authenticated"));
    }

    @Test
    @WithMockUser
    void findById_notFound() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var exception = new ScheduleNotFoundException(id);
        // When
        when(manager.findByIdForCurrentUser(any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(get("/api/schedule/{id}", id)
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Schedule with ID '" + id + "' does not exist"));
    }

    @Test
    void findById_unauthorized() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(get("/api/schedule/{id}", id)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
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