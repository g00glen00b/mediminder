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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleController.class)
@Import(SecurityConfiguration.class)
class ScheduleControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private ScheduleManager manager;

    @Nested
    class findAll {
        @Test
        void returnsResults() throws Exception {
            var schedule = new ScheduleDTO(
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
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2025, 6, 1),
                    null
                ),
                "Before lunch",
                BigDecimal.ONE,
                LocalTime.of(11, 0)
            );
            var pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
            var page = new PageImpl<>(List.of(schedule));
            when(manager.findAllForCurrentUser(null, false, pageRequest)).thenReturn(page);
            mvc
                .perform(get("/api/schedule")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc")
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(schedule.id().toString()));
        }
    }

    @Nested
    class create {
        @Test
        void returnsResult() throws Exception {
            var schedule = new ScheduleDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MedicationDTO(
                    UUID.fromString("a0eebc4b-1f3d-4b8e-9f2c-5d6f7a8b9c0d"),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2025, 6, 1),
                    null
                ),
                "Before lunch",
                BigDecimal.ONE,
                LocalTime.of(11, 0)
            );
            var request = new CreateScheduleRequestDTO(
                schedule.medication().id(),
                schedule.interval(),
                schedule.period(),
                schedule.time(),
                schedule.description(),
                schedule.dose()
            );
            var json = """
            {
                "medicationId": "a0eebc4b-1f3d-4b8e-9f2c-5d6f7a8b9c0d",
                "interval": "P1D",
                "period": {
                    "startingAt": "2025-06-01",
                    "endingAtInclusive": null
                },
                "description": "Before lunch",
                "dose": 1,
                "time": "11:00"
            }
            """;
            when(manager.createForCurrentUser(request)).thenReturn(schedule);
            mvc
                .perform(post("/api/schedule")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(schedule.id().toString()));
        }

        @Test
        void returnsInvalid() throws Exception {
            var request = new CreateScheduleRequestDTO(
                UUID.fromString("a0eebc4b-1f3d-4b8e-9f2c-5d6f7a8b9c0d"),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2025, 6, 1),
                    null
                ),
                LocalTime.of(11, 0),
                "Before lunch",
                BigDecimal.ONE
            );
            var json = """
            {
                "medicationId": "a0eebc4b-1f3d-4b8e-9f2c-5d6f7a8b9c0d",
                "interval": "P1D",
                "period": {
                    "startingAt": "2025-06-01",
                    "endingAtInclusive": null
                },
                "description": "Before lunch",
                "dose": 1,
                "time": "11:00"
            }
            """;
            var exception = new InvalidScheduleException("Invalid schedule");
            when(manager.createForCurrentUser(request)).thenThrow(exception);
            mvc
                .perform(post("/api/schedule")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid schedule"))
                .andExpect(jsonPath("$.type").value("https://mediminder/schedule/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var request = new CreateScheduleRequestDTO(
                UUID.fromString("a0eebc4b-1f3d-4b8e-9f2c-5d6f7a8b9c0d"),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2025, 6, 1),
                    null
                ),
                LocalTime.of(11, 0),
                "Before lunch",
                BigDecimal.ONE
            );
            var json = """
            {
                "medicationId": "a0eebc4b-1f3d-4b8e-9f2c-5d6f7a8b9c0d",
                "interval": "P1D",
                "period": {
                    "startingAt": "2025-06-01",
                    "endingAtInclusive": null
                },
                "description": "Before lunch",
                "dose": 1,
                "time": "11:00"
            }
            """;
            var exception = new ConstraintViolationException("Invalid schedule", null);
            when(manager.createForCurrentUser(request)).thenThrow(exception);
            mvc
                .perform(post("/api/schedule")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid schedule"))
                .andExpect(jsonPath("$.type").value("https://mediminder/schedule/invalid"))
                .andExpect(jsonPath("$.detail").value("Validation failed"));
        }
    }

    @Nested
    class update {
        @Test
        void returnsResult() throws Exception {
            var schedule = new ScheduleDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MedicationDTO(
                    UUID.fromString("a0eebc4b-1f3d-4b8e-9f2c-5d6f7a8b9c0d"),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2025, 6, 1),
                    null
                ),
                "Before lunch",
                BigDecimal.ONE,
                LocalTime.of(11, 0)
            );
            var request = new UpdateScheduleRequestDTO(
                schedule.interval(),
                schedule.period(),
                schedule.time(),
                schedule.description(),
                schedule.dose()
            );
            var json = """
                {
                    "interval": "P1D",
                    "period": {
                        "startingAt": "2025-06-01",
                        "endingAtInclusive": null
                    },
                    "description": "Before lunch",
                    "dose": 1,
                    "time": "11:00"
                }
                """;
            when(manager.updateForCurrentUser(schedule.id(), request)).thenReturn(schedule);
            mvc
                .perform(put("/api/schedule/{id}", schedule.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(schedule.id().toString()));
        }

        @Test
        void returnsNotFound() throws Exception {
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2025, 6, 1),
                    null
                ),
                LocalTime.of(11, 0),
                "Before lunch",
                BigDecimal.ONE
            );
            var json = """
                {
                    "interval": "P1D",
                    "period": {
                        "startingAt": "2025-06-01",
                        "endingAtInclusive": null
                    },
                    "description": "Before lunch",
                    "dose": 1,
                    "time": "11:00"
                }
                """;
            UUID id = UUID.randomUUID();
            var exception = new ScheduleNotFoundException(id);
            when(manager.updateForCurrentUser(id, request)).thenThrow(exception);
            mvc
                .perform(put("/api/schedule/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Schedule not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/schedule/notfound"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsInvalid() throws Exception {
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2025, 6, 1),
                    null
                ),
                LocalTime.of(11, 0),
                "Before lunch",
                BigDecimal.ONE
            );
            var json = """
                {
                    "interval": "P1D",
                    "period": {
                        "startingAt": "2025-06-01",
                        "endingAtInclusive": null
                    },
                    "description": "Before lunch",
                    "dose": 1,
                    "time": "11:00"
                }
                """;
            UUID id = UUID.randomUUID();
            var exception = new InvalidScheduleException("Invalid schedule");
            when(manager.updateForCurrentUser(id, request)).thenThrow(exception);
            mvc
                .perform(put("/api/schedule/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid schedule"))
                .andExpect(jsonPath("$.type").value("https://mediminder/schedule/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var request = new UpdateScheduleRequestDTO(
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2025, 6, 1),
                    null
                ),
                LocalTime.of(11, 0),
                "Before lunch",
                BigDecimal.ONE
            );
            var json = """
                {
                    "interval": "P1D",
                    "period": {
                        "startingAt": "2025-06-01",
                        "endingAtInclusive": null
                    },
                    "description": "Before lunch",
                    "dose": 1,
                    "time": "11:00"
                }
                """;
            UUID id = UUID.randomUUID();
            var exception = new ConstraintViolationException("Invalid schedule", null);
            when(manager.updateForCurrentUser(id, request)).thenThrow(exception);
            mvc
                .perform(put("/api/schedule/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid schedule"))
                .andExpect(jsonPath("$.type").value("https://mediminder/schedule/invalid"))
                .andExpect(jsonPath("$.detail").value("Validation failed"));
        }
    }

    @Nested
    class delete {
        @Test
        void deletes() throws Exception {
            var id = UUID.randomUUID();
            mvc
                .perform(delete("/api/schedule/{id}", id)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).deleteForCurrentUser(id);
        }

        @Test
        void returnsNotFound() throws Exception {
            var id = UUID.randomUUID();
            var exception = new ScheduleNotFoundException(id);
            doThrow(exception).when(manager).deleteForCurrentUser(id);
            mvc
                .perform(delete("/api/schedule/{id}", id)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Schedule not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/schedule/notfound"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class findById {
        @Test
        void returnsResult() throws Exception {
            var schedule = new ScheduleDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MedicationDTO(
                    UUID.fromString("a0eebc4b-1f3d-4b8e-9f2c-5d6f7a8b9c0d"),
                    "Dafalgan",
                    new MedicationTypeDTO("TABLET", "Tablet"),
                    new AdministrationTypeDTO("ORAL", "Oral"),
                    new DoseTypeDTO("TABLET", "tablet(s)"),
                    new BigDecimal("100"),
                    Color.RED
                ),
                Period.ofDays(1),
                new SchedulePeriodDTO(
                    LocalDate.of(2025, 6, 1),
                    null
                ),
                "Before lunch",
                BigDecimal.ONE,
                LocalTime.of(11, 0)
            );
            when(manager.findByIdForCurrentUser(schedule.id())).thenReturn(schedule);
            mvc
                .perform(get("/api/schedule/{id}", schedule.id())
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(schedule.id().toString()));
        }

        @Test
        void returnsNotFound() throws Exception {
            var id = UUID.randomUUID();
            var exception = new ScheduleNotFoundException(id);
            when(manager.findByIdForCurrentUser(id)).thenThrow(exception);
            mvc
                .perform(get("/api/schedule/{id}", id)
                    .with(user("me@example.org")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Schedule not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/schedule/notfound"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }
}