package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.user.*;
import codes.dimitri.mediminder.api.user.implementation.cleanup.UserCodeCleanupBatchTask;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@EnableMethodSecurity
@Import(SecurityConfiguration.class)
class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private UserManager manager;
    @MockitoBean
    private UserCodeCleanupBatchTask task;

    @Nested
    class register {
        @Test
        void returnsResult() throws Exception {
            var request = new RegisterUserRequestDTO(
                "me@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var json = """
            {
                "email": "me@example.org",
                "password": "password",
                "name": "Harry Potter",
                "timezone": "Europe/Brussels"
            }
            """;
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                false,
                false
            );
            when(manager.register(request)).thenReturn(user);
            mvc
                .perform(post("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(user.id().toString()))
                .andExpect(jsonPath("$.name").value(user.name()))
                .andExpect(jsonPath("$.timezone").value(user.timezone().getId()))
                .andExpect(jsonPath("$.enabled").value(user.enabled()))
                .andExpect(jsonPath("$.admin").value(user.admin()));
        }

        @Test
        void returnsInvalid() throws Exception {
            var request = new RegisterUserRequestDTO(
                "me@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var json = """
            {
                "email": "me@example.org",
                "password": "password",
                "name": "Harry Potter",
                "timezone": "Europe/Brussels"
            }
            """;
            var exception = new InvalidUserException("Invalid user");
            when(manager.register(request)).thenThrow(exception);
            mvc
                .perform(post("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsMailFailed() throws Exception {
            var request = new RegisterUserRequestDTO(
                "me@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var json = """
            {
                "email": "me@example.org",
                "password": "password",
                "name": "Harry Potter",
                "timezone": "Europe/Brussels"
            }
            """;
            var exception = new UserMailFailedException("Sending mail failed", new RuntimeException());
            when(manager.register(request)).thenThrow(exception);
            mvc
                .perform(post("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Sending mail failed"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/internal/mail"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var request = new RegisterUserRequestDTO(
                "me@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var json = """
            {
                "email": "me@example.org",
                "password": "password",
                "name": "Harry Potter",
                "timezone": "Europe/Brussels"
            }
            """;
            var exception = new ConstraintViolationException("Validation failed", null);
            when(manager.register(request)).thenThrow(exception);
            mvc
                .perform(post("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsCodeGenerationFailed() throws Exception {
            var request = new RegisterUserRequestDTO(
                "me@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var json = """
            {
                "email": "me@example.org",
                "password": "password",
                "name": "Harry Potter",
                "timezone": "Europe/Brussels"
            }
            """;
            var exception = new UserCodeGenerationException("Generating unique code for user failed");
            when(manager.register(request)).thenThrow(exception);
            mvc
                .perform(post("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Generating unique code for user failed"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/internal/code"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class verify {
        @Test
        void returnsResult() throws Exception {
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                false,
                false
            );
            var verificationCode = "verificationCode";
            when(manager.verify(verificationCode)).thenReturn(user);
            mvc
                .perform(post("/api/user/verify")
                    .param("code", verificationCode)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id().toString()))
                .andExpect(jsonPath("$.name").value(user.name()))
                .andExpect(jsonPath("$.timezone").value(user.timezone().getId()))
                .andExpect(jsonPath("$.enabled").value(user.enabled()))
                .andExpect(jsonPath("$.admin").value(user.admin()));
        }

        @Test
        void returnsInvalidCode() throws Exception {
            var verificationCode = "verificationCode";
            var exception = new InvalidUserException("There is no user with this verification code");
            when(manager.verify(verificationCode)).thenThrow(exception);
            mvc
                .perform(post("/api/user/verify")
                    .param("code", verificationCode)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class resetVerification {
        @Test
        void resetsVerification() throws Exception {
            var email = "me@example.org";
            mvc
                .perform(post("/api/user/verify/reset")
                    .param("email", email)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).resetVerification(email);
        }

        @Test
        void invalidUser() throws Exception {
            var email = "me@example.org";
            var exception = new InvalidUserException("Invalid user");
            doThrow(exception).when(manager).resetVerification(email);
            mvc
                .perform(post("/api/user/verify/reset")
                    .param("email", email)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class findCurrentUser {
        @Test
        void returnsResult() throws Exception {
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                false,
                false
            );
            when(manager.findCurrentUser()).thenReturn(user);
            mvc
                .perform(get("/api/user/current")
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id().toString()))
                .andExpect(jsonPath("$.name").value(user.name()))
                .andExpect(jsonPath("$.timezone").value(user.timezone().getId()))
                .andExpect(jsonPath("$.enabled").value(user.enabled()))
                .andExpect(jsonPath("$.admin").value(user.admin()));
        }
    }

    @Nested
    class findAvailableTimezones {
        @Test
        void returnsResults() throws Exception {
            var timezones = List.of("Europe/Brussels");
            var search = "Europe";
            when(manager.findAvailableTimezones(search)).thenReturn(timezones);
            mvc
                .perform(get("/api/user/timezone")
                    .param("search", search)
                    .with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Europe/Brussels"));
        }
    }

    @Nested
    class update {
        @Test
        void returnsResult() throws Exception {
            var request = new UpdateUserRequestDTO(
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                false,
                false
            );
            var json = """
            {
                "name": "Harry Potter",
                "timezone": "Europe/Brussels"
            }
            """;
            when(manager.update(request)).thenReturn(user);
            mvc
                .perform(put("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id().toString()));
        }

        @Test
        void returnsInvalidUser() throws Exception {
            var request = new UpdateUserRequestDTO(
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var json = """
            {
                "name": "Harry Potter",
                "timezone": "Europe/Brussels"
            }
            """;
            var exception = new InvalidUserException("Invalid user");
            when(manager.update(request)).thenThrow(exception);
            mvc
                .perform(put("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var request = new UpdateUserRequestDTO(
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            var json = """
            {
                "name": "Harry Potter",
                "timezone": "Europe/Brussels"
            }
            """;
            var exception = new ConstraintViolationException("Validation failed", null);
            when(manager.update(request)).thenThrow(exception);
            mvc
                .perform(put("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class updateCredentials {
        @Test
        void returnsResult() throws Exception {
            var request = new UpdateCredentialsRequestDTO(
                "oldPassword",
                "newPassword"
            );
            var user = new UserDTO(
                UUID.randomUUID(),
                "Harry Potter",
                ZoneId.of("Europe/Brussels"),
                false,
                false
            );
            var json = """
            {
                "oldPassword": "oldPassword",
                "newPassword": "newPassword"
            }
            """;
            when(manager.updateCredentials(request)).thenReturn(user);
            mvc
                .perform(put("/api/user/credentials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id().toString()));
        }

        @Test
        void invalidUser() throws Exception {
            var request = new UpdateCredentialsRequestDTO(
                "oldPassword",
                "newPassword"
            );
            var json = """
            {
                "oldPassword": "oldPassword",
                "newPassword": "newPassword"
            }
            """;
            var exception = new InvalidUserException("Invalid user");
            when(manager.updateCredentials(request)).thenThrow(exception);
            mvc
                .perform(put("/api/user/credentials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void constraintViolation() throws Exception {
            var request = new UpdateCredentialsRequestDTO(
                "oldPassword",
                "newPassword"
            );
            var json = """
            {
                "oldPassword": "oldPassword",
                "newPassword": "newPassword"
            }
            """;
            var exception = new ConstraintViolationException("Validation failed", null);
            when(manager.updateCredentials(request)).thenThrow(exception);
            mvc
                .perform(put("/api/user/credentials")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class requestResetCredentials {
        @Test
        void returnsResult() throws Exception {
            var email = "me@example.org";
            mvc
                .perform(post("/api/user/credentials/reset/request")
                    .param("email", email)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).requestResetCredentials(email);
        }

        @Test
        void returnsInvalidUser() throws Exception {
            var email = "me@example.org";
            var exception = new InvalidUserException("Invalid user");
            doThrow(exception).when(manager).requestResetCredentials(email);
            mvc
                .perform(post("/api/user/credentials/reset/request")
                    .param("email", email)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsMailFailed() throws Exception {
            var email = "me@example.org";
            var exception = new UserMailFailedException("Sending mail failed", new RuntimeException());
            doThrow(exception).when(manager).requestResetCredentials(email);
            mvc
                .perform(post("/api/user/credentials/reset/request")
                    .param("email", email)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Sending mail failed"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/internal/mail"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsCodeGenerationException() throws Exception {
            var email = "me@example.org";
            var exception = new UserCodeGenerationException("Generating unique code for user failed");
            doThrow(exception).when(manager).requestResetCredentials(email);
            mvc
                .perform(post("/api/user/credentials/reset/request")
                    .param("email", email)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Generating unique code for user failed"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/internal/code"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class confirmResetCredentials {
        @Test
        void resetsCredentials() throws Exception {
            var request = new ResetCredentialsRequestDTO("passwordResetCode", "newPassword");
            var json = """
            {
                "passwordResetCode": "passwordResetCode",
                "newPassword": "newPassword"
            }
            """;
            mvc
                .perform(post("/api/user/credentials/reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).resetCredentials(request);
        }

        @Test
        void returnsInvalidUser() throws Exception {
            var request = new ResetCredentialsRequestDTO("passwordResetCode", "newPassword");
            var json = """
            {
                "passwordResetCode": "passwordResetCode",
                "newPassword": "newPassword"
            }
            """;
            var exception = new InvalidUserException("Invalid user");
            doThrow(exception).when(manager).resetCredentials(request);
            mvc
                .perform(post("/api/user/credentials/reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }

        @Test
        void returnsConstraintViolation() throws Exception {
            var request = new ResetCredentialsRequestDTO("passwordResetCode", "newPassword");
            var json = """
            {
                "passwordResetCode": "passwordResetCode",
                "newPassword": "newPassword"
            }
            """;
            var exception = new ConstraintViolationException("Validation failed", null);
            doThrow(exception).when(manager).resetCredentials(request);
            mvc
                .perform(post("/api/user/credentials/reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(anonymous())
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class launchCodeCleanupJob {
        @Test
        void launchesJob() throws Exception {
            mvc
                .perform(post("/api/user/batch/unused-code/start")
                    .with(user("me@example.org")
                        .authorities(new SimpleGrantedAuthority("ADMIN")))
                    .with(csrf()))
                .andExpect(status().isAccepted());
            verify(task).run();
        }

        @Test
        void forbidden() throws Exception {
            mvc
                .perform(post("/api/user/batch/unused-code/start")
                    .with(user("me@example.org"))
                    .with(csrf()))
                .andExpect(status().isForbidden());
            verifyNoInteractions(task);
        }
    }
}