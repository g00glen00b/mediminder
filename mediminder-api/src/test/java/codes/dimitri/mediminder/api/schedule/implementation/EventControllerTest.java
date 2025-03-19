package codes.dimitri.mediminder.api.schedule.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.medication.*;
import codes.dimitri.mediminder.api.schedule.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(SecurityConfiguration.class)
class EventControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private EventManager manager;

    @Nested
    class findAll {
        @Test
        void returnsResults() throws Exception {
            var event = new EventDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MedicationDTO(
                    UUID.randomUUID(),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                LocalDateTime.of(2025, 6, 1, 10, 0),
                LocalDateTime.of(2025, 6, 1, 10, 1),
                BigDecimal.ONE,
                "Taken before lunch"
            );
            var date = LocalDate.of(2025, 6, 1);
            when(manager.findAll(date)).thenReturn(List.of(event));
            mvc
                .perform(get("/api/event/{targetDate}", date)
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(event.id().toString()));
        }
    }

    @Nested
    class complete {
        @Test
        void returnsResult() throws Exception {
            var event = new EventDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MedicationDTO(
                    UUID.randomUUID(),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                LocalDateTime.of(2025, 6, 1, 10, 0),
                LocalDateTime.of(2025, 6, 1, 10, 1),
                BigDecimal.ONE,
                "Taken before lunch"
            );
            var date = LocalDate.of(2025, 6, 1);
            when(manager.complete(event.scheduleId(), date)).thenReturn(event);
            mvc
                .perform(post("/api/schedule/{scheduleId}/event/{targetDate}", event.scheduleId(), date)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(event.id().toString()));
        }

        @Test
        void returnsNotFound() throws Exception {
            var date = LocalDate.of(2025, 6, 1);
            UUID scheduleId = UUID.randomUUID();
            var exception = new EventNotFoundException(scheduleId, date);
            when(manager.complete(scheduleId, date)).thenThrow(exception);
            mvc
                .perform(post("/api/schedule/{scheduleId}/event/{targetDate}", scheduleId, date)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Event not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/event/notfound"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsInvalid() throws Exception {
            var date = LocalDate.of(2025, 6, 1);
            UUID scheduleId = UUID.randomUUID();
            var exception = new InvalidEventException("Invalid event");
            when(manager.complete(scheduleId, date)).thenThrow(exception);
            mvc
                .perform(post("/api/schedule/{scheduleId}/event/{targetDate}", scheduleId, date)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid event"))
                .andExpect(jsonPath("$.type").value("https://mediminder/event/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var date = LocalDate.of(2025, 6, 1);
            UUID scheduleId = UUID.randomUUID();
            var exception = new ConstraintViolationException("Constraint violation", null);
            when(manager.complete(scheduleId, date)).thenThrow(exception);
            mvc
                .perform(post("/api/schedule/{scheduleId}/event/{targetDate}", scheduleId, date)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid event"))
                .andExpect(jsonPath("$.type").value("https://mediminder/event/invalid"))
                .andExpect(jsonPath("$.detail").value("Validation failed"));
        }
    }

    @Nested
    class delete {
        @Test
        void returnsNoContent() throws Exception {
            var eventId = UUID.randomUUID();
            mvc
                .perform(delete("/api/event/{eventId}", eventId)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).uncomplete(eventId);
        }

        @Test
        void returnsNotFound() throws Exception {
            var eventId = UUID.randomUUID();
            var exception = new CompletedEventNotFoundException(eventId);
            doThrow(exception).when(manager).uncomplete(eventId);
            mvc
                .perform(delete("/api/event/{eventId}", eventId)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Event not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/event/notfound"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }
}