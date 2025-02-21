package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.notification.*;
import codes.dimitri.mediminder.api.notification.implementation.batch.NotificationBatchTask;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import(SecurityConfiguration.class)
class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private NotificationManager manager;
    @MockBean
    private NotificationBatchTask task;

    @Test
    @WithMockUser
    void findAll_ok() throws Exception {
        // Given
        var notification = Instancio.create(NotificationDTO.class);
        // When
        when(manager.findAll(any())).thenReturn(new PageImpl<>(List.of(notification)));
        // Then
        mockMvc
            .perform(get("/api/notification")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(notification.id().toString()));
        verify(manager).findAll(PageRequest.of(0, 10, Sort.Direction.ASC, "id"));
    }

    @Test
    void findAll_unauthorized() throws Exception {
        mockMvc
            .perform(get("/api/notification")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void findAll_invalidNotification() throws Exception {
        // Given
        var exception = new InvalidNotificationException("User is not authenticated");
        // When
        when(manager.findAll(any())).thenThrow(exception);
        // Then
        mockMvc
            .perform(get("/api/notification")
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sort", "id,asc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User is not authenticated"));
    }

    @Test
    @WithMockUser
    void delete_ok() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/notification/{id}", id)
                .with(csrf()))
            .andExpect(status().isOk());
        verify(manager).delete(id);
    }

    @Test
    void delete_unauthorized() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/notification/{id}", id)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    void delete_withoutCsrf() throws Exception {
        // Given
        var id = UUID.randomUUID();
        // Then
        mockMvc
            .perform(delete("/api/notification/{id}", id))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void delete_invalid() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var exception = new InvalidNotificationException("User is not authenticated");
        // When
        doThrow(exception).when(manager).delete(any());
        // Then
        mockMvc
            .perform(delete("/api/notification/{id}", id)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User is not authenticated"));
        verify(manager).delete(id);
    }

    @Test
    @WithMockUser
    void delete_notFound() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var exception = new NotificationNotFoundException(id);
        // When
        doThrow(exception).when(manager).delete(any());
        // Then
        mockMvc
            .perform(delete("/api/notification/{id}", id)
                .with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Notification with ID '" + id + "' does not exist"));
        verify(manager).delete(id);
    }

    @Test
    @WithMockUser
    void subscribe_ok() throws Exception {
        // Given
        var request = """
        {
            "endpoint": "https://example.org",
            "keys": {
                "p256dh": "key1",
                "auth": "key2"
            }
        }
        """;
        // Then
        mockMvc
            .perform(post("/api/notification/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk());
        verify(manager).subscribe(new CreateSubscriptionRequestDTO(
           "https://example.org",
           new SubscriptionKeysDTO("key1", "key2")
        ));
    }

    @Test
    void subscribe_unauthorized() throws Exception {
        // Given
        var request = """
        {
            "endpoint": "https://example.org",
            "keys": {
                "p256dh": "key1",
                "auth": "key2"
            }
        }
        """;
        // Then
        mockMvc
            .perform(post("/api/notification/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void subscribe_withoutCsrf() throws Exception {
        // Given
        var request = """
        {
            "endpoint": "https://example.org",
            "keys": {
                "p256dh": "key1",
                "auth": "key2"
            }
        }
        """;
        // Then
        mockMvc
            .perform(post("/api/notification/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void subscribe_invalid() throws Exception {
        // Given
        var request = """
        {
            "endpoint": "https://example.org",
            "keys": {
                "p256dh": "key1",
                "auth": "key2"
            }
        }
        """;
        var exception = new InvalidNotificationException("User is not authenticated");
        // When
        doThrow(exception).when(manager).subscribe(any());
        // Then
        mockMvc
            .perform(post("/api/notification/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User is not authenticated"));
    }

    @Test
    @WithMockUser
    void unsubscribe_ok() throws Exception {
        mockMvc
            .perform(delete("/api/notification/subscription")
                .with(csrf()))
            .andExpect(status().isOk());
        verify(manager).unsubscribe();
    }

    @Test
    void unsubscribe_unauthorized() throws Exception {
        mockMvc
            .perform(delete("/api/notification/subscription")
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    void unsubscribe_withoutCsrf() throws Exception {
        mockMvc
            .perform(delete("/api/notification/subscription"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void unsubscribe_invalid() throws Exception {
        // Given
        var exception = new InvalidNotificationException("User is not authenticated");
        // When
        doThrow(exception).when(manager).unsubscribe();
        // Then
        mockMvc
            .perform(delete("/api/notification/subscription")
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User is not authenticated"));
    }

    @Test
    @WithMockUser
    void findConfiguration_ok() throws Exception {
        // Given
        var config = Instancio.create(SubscriptionConfigurationDTO.class);
        // When
        when(manager.findConfiguration()).thenReturn(config);
        // Then
        mockMvc
            .perform(get("/api/notification/subscription/configuration"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.publicKey").value(config.publicKey()));
        verify(manager).findConfiguration();
    }

    @Test
    void findConfiguration_unauthorized() throws Exception {
        mockMvc
            .perform(get("/api/notification/subscription/configuration"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void launchJob_ok() throws Exception {
        mockMvc
            .perform(post("/api/notification/batch/start")
                .with(csrf()))
            .andExpect(status().isOk());
        verify(task).run();
    }

    @Test
    void launchJob_unauthorized() throws Exception {
        mockMvc
            .perform(post("/api/notification/batch/start")
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(task);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void launchJob_withoutCsrf() throws Exception {
        mockMvc
            .perform(post("/api/notification/batch/start"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(task);
    }

    @Test
    @WithMockUser
    void launchJob_noAdminAuthority() throws Exception {
        mockMvc
            .perform(post("/api/notification/batch/start")
                .with(csrf()))
            .andExpect(status().isForbidden());
        verifyNoInteractions(task);
    }
}