package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.notification.*;
import codes.dimitri.mediminder.api.notification.implementation.batch.NotificationBatchTask;
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@EnableMethodSecurity
@Import(SecurityConfiguration.class)
class NotificationControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private NotificationManager manager;
    @MockitoBean
    private NotificationBatchTask task;

    @Nested
    class findAll {
        @Test
        void returnsResults() throws Exception {
            var notification = new NotificationDTO(
                UUID.randomUUID(),
                NotificationType.CABINET_ENTRY_EXPIRED,
                "Cabinet entry expired",
                "Dafalgan is expired"
            );
            var pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
            var page = new PageImpl<>(List.of(notification));
            when(manager.findAll(pageRequest)).thenReturn(page);
            mvc
                .perform(get("/api/notification")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "id,asc")
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(notification.id().toString()))
                .andExpect(jsonPath("$.content[0].type").value(notification.type().name()))
                .andExpect(jsonPath("$.content[0].title").value(notification.title()))
                .andExpect(jsonPath("$.content[0].message").value(notification.message()));
        }
    }

    @Nested
    class delete {
        @Test
        void deletesNotification() throws Exception {
            var id = UUID.randomUUID();
            mvc
                .perform(delete("/api/notification/{id}", id)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).delete(id);
        }

        @Test
        void returnsNotFound() throws Exception {
            var id = UUID.randomUUID();
            var exception = new NotificationNotFoundException(id);
            doThrow(exception).when(manager).delete(id);
            mvc
                .perform(delete("/api/notification/{id}", id)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Notification not found"))
                .andExpect(jsonPath("$.type").value("https://mediminder/notification/notfound"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsInvalid() throws Exception {
            var id = UUID.randomUUID();
            var exception = new InvalidNotificationException("Invalid notification");
            doThrow(exception).when(manager).delete(id);
            mvc
                .perform(delete("/api/notification/{id}", id)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid notification"))
                .andExpect(jsonPath("$.type").value("https://mediminder/notification/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class subscribe {
        @Test
        void subscribes() throws Exception {
            var request = new CreateSubscriptionRequestDTO(
                "https://example.org/notification",
                new SubscriptionKeysDTO("key1", "key2")
            );
            var json = """
            {
                "endpoint": "https://example.org/notification",
                "keys": {
                    "p256dh": "key1",
                    "auth": "key2"
                }
            }
            """;
            mvc
                .perform(post("/api/notification/subscription")
                    .with(user("me@example.org"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isCreated());
            verify(manager).subscribe(request);
        }

        @Test
        void handleConstraintViolation() throws Exception {
            var exception = new ConstraintViolationException("Constraint violation", null);
            var json = """
            {
                "endpoint": "https://example.org/notification",
                "keys": {
                    "p256dh": "key1",
                    "auth": "key2"
                }
            }
            """;
            doThrow(exception).when(manager).subscribe(any());
            mvc
                .perform(post("/api/notification/subscription")
                    .with(user("me@example.org"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid notification"))
                .andExpect(jsonPath("$.type").value("https://mediminder/notification/invalid"))
                .andExpect(jsonPath("$.detail").value("Validation failed"));
        }

        @Test
        void invalid() throws Exception {
            var exception = new InvalidNotificationException("Invalid notification");
            var json = """
            {
                "endpoint": "https://example.org/notification",
                "keys": {
                    "p256dh": "key1",
                    "auth": "key2"
                }
            }
            """;
            doThrow(exception).when(manager).subscribe(any());
            mvc
                .perform(post("/api/notification/subscription")
                    .with(user("me@example.org"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid notification"))
                .andExpect(jsonPath("$.type").value("https://mediminder/notification/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class unsubscribe {
        @Test
        void unsubscirbes() throws Exception {
            mvc
                .perform(delete("/api/notification/subscription")
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).unsubscribe();
        }

        @Test
        void invalid() throws Exception {
            var exception = new InvalidNotificationException("Invalid notification");
            doThrow(exception).when(manager).unsubscribe();
            mvc
                .perform(delete("/api/notification/subscription")
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid notification"))
                .andExpect(jsonPath("$.type").value("https://mediminder/notification/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class findConfiguration {
        @Test
        void returnsResult() throws Exception {
            var config = new SubscriptionConfigurationDTO("publicKey");
            when(manager.findConfiguration()).thenReturn(config);
            mvc
                .perform(get("/api/notification/subscription/configuration")
                    .with(user("me@example.org")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicKey").value(config.publicKey()));
        }
    }

    @Nested
    class launchJob {
        @Test
        void launchesJob() throws Exception {
            mvc
                .perform(post("/api/notification/batch/start")
                    .with(user("me@example.org")
                        .authorities(new SimpleGrantedAuthority("ADMIN")))
                    .with(csrf()))
                .andExpect(status().isAccepted());
            verify(task).run();
        }

        @Test
        void failsIfNotAdmin() throws Exception {
            mvc
                .perform(post("/api/notification/batch/start")
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isForbidden());
            verifyNoInteractions(task);
        }
    }
}