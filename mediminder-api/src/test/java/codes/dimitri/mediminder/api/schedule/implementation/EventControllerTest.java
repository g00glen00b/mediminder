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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(controllers = EventController.class)
@Import(SecurityConfiguration.class)
class EventControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EventManager eventManager;

    @Test
    @WithMockUser
    void findAll_ok() throws Exception {
        // Given
        var event = Instancio.create(EventDTO.class);
        // When
        when(eventManager.findAll(any())).thenReturn(List.of(event));
        // Then
        mockMvc
            .perform(get("/api/event/{targetDate}", event.targetDate().toLocalDate()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(event.id().toString()));
        verify(eventManager).findAll(event.targetDate().toLocalDate());
    }

    @Test
    void findAll_unauthorized() throws Exception {
        // Given
        var date = LocalDate.of(2024, 6, 30);
        // Then
        mockMvc
            .perform(get("/api/event/{targetDate}", date))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(eventManager);
    }

    @Test
    @WithMockUser
    void findAll_invalidEvent() throws Exception {
        // Given
        var date = LocalDate.of(2024, 6, 30);
        var exception = new InvalidEventException("User is not authenticated");
        // When
        when(eventManager.findAll(any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(get("/api/event/{targetDate}", date))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User is not authenticated"));
    }

    @Test
    @WithMockUser
    void complete_ok() throws Exception {
        // Given
        var event = Instancio.create(EventDTO.class);
        UUID scheduleId = event.scheduleId();
        LocalDate targetDate = event.targetDate().toLocalDate();
        // When
        when(eventManager.complete(any(), any())).thenReturn(event);
        // Then
        mockMvc
            .perform(post("/api/schedule/{scheduleId}/event/{targetDate}", scheduleId, targetDate)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(event.id().toString()));
        verify(eventManager).complete(scheduleId, targetDate);
    }

    @Test
    @WithMockUser
    void complete_invalidEvent() throws Exception {
        // Given
        var targetDate = LocalDate.of(2024, 6, 30);
        var scheduleId = UUID.randomUUID();
        var exception = new InvalidEventException("User is not authenticated");
        // When
        when(eventManager.complete(any(), any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(post("/api/schedule/{scheduleId}/event/{targetDate}", scheduleId, targetDate)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User is not authenticated"));
        verify(eventManager).complete(scheduleId, targetDate);
    }

    @Test
    @WithMockUser
    void complete_constraintViolation() throws Exception {
        // Given
        var targetDate = LocalDate.of(2024, 6, 30);
        var scheduleId = UUID.randomUUID();
        var violation = new MessageConstraintViolation("Test");
        var exception = new ConstraintViolationException(Set.of(violation));
        // When
        when(eventManager.complete(any(), any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(post("/api/schedule/{scheduleId}/event/{targetDate}", scheduleId, targetDate)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Test"));
        verify(eventManager).complete(scheduleId, targetDate);
    }

    @Test
    @WithMockUser
    void complete_notFound() throws Exception {
        // Given
        var targetDate = LocalDate.of(2024, 6, 30);
        var scheduleId = UUID.randomUUID();
        var exception = new EventNotFoundException(scheduleId, targetDate);
        // When
        when(eventManager.complete(any(), any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(post("/api/schedule/{scheduleId}/event/{targetDate}", scheduleId, targetDate)
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Schedule '" + scheduleId + "' is not expected to be taken at '" + targetDate + "'"));
        verify(eventManager).complete(scheduleId, targetDate);
    }

    @Test
    @WithMockUser
    void complete_withoutCsrf() throws Exception {
        // Given
        var targetDate = LocalDate.of(2024, 6, 30);
        var scheduleId = UUID.randomUUID();
        // Then
        mockMvc
            .perform(post("/api/schedule/{scheduleId}/event/{targetDate}", scheduleId, targetDate))
            .andExpect(status().isForbidden());
        verifyNoInteractions(eventManager);
    }

    @Test
    void complete_unauthorized() throws Exception {
        // Given
        var targetDate = LocalDate.of(2024, 6, 30);
        var scheduleId = UUID.randomUUID();
        // Then
        mockMvc
            .perform(post("/api/schedule/{scheduleId}/event/{targetDate}", scheduleId, targetDate)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(eventManager);
    }

    @Test
    @WithMockUser
    void delete_ok() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/event/{id}", id)
                .with(csrf()))
            .andExpect(status().isOk());
        verify(eventManager).delete(id);
    }

    @Test
    @WithMockUser
    void delete_invalidEvent() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var exception = new InvalidEventException("User is not authenticated");
        // When
        doThrow(exception).when(eventManager).delete(any());
        // Then
        mockMvc
            .perform(delete("/api/event/{id}", id)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User is not authenticated"));
        verify(eventManager).delete(id);
    }

    @Test
    @WithMockUser
    void delete_notFound() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var exception = new CompletedEventNotFoundException(id);
        // When
        doThrow(exception).when(eventManager).delete(any());
        // Then
        mockMvc
            .perform(delete("/api/event/{id}", id)
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Completed event with ID '" + id + "' does not exist"));
        verify(eventManager).delete(id);
    }

    @Test
    @WithMockUser
    void delete_withoutCsrf() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/event/{id}", id))
            .andExpect(status().isForbidden());
        verifyNoInteractions(eventManager);
    }

    @Test
    void delete_unauthorized() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/event/{id}", id)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(eventManager);
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