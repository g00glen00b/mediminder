package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.common.SecurityConfiguration;
import codes.dimitri.mediminder.api.user.*;
import codes.dimitri.mediminder.api.user.implementation.cleanup.UserCodeCleanupBatchTask;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfiguration.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserManager manager;
    @MockBean
    private UserCodeCleanupBatchTask task;

    @Test
    void register_ok() throws Exception {
        // Given
        var request = """
            {
                "email": "me@example.org",
                "name": "Jane Doe",
                "password": "p@$$w0rd",
                "timezone": "UTC"
            }
        """;
        var user = Instancio.create(UserDTO.class);
        // When
        when(manager.register(any())).thenReturn(user);
        // Then
        mockMvc
            .perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.id().toString()));
        verify(manager).register(new RegisterUserRequestDTO(
            "me@example.org",
            "p@$$w0rd",
            "Jane Doe",
            ZoneId.of("UTC")
        ));
    }

    @Test
    void register_withoutCsrf() throws Exception {
        // Given
        var request = """
            {
                "email": "me@example.org",
                "name": "Jane Doe",
                "password": "p@$$w0rd",
                "timezone": "UTC"
            }
        """;
        // Then
        mockMvc
            .perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    void register_mailFailed() throws Exception {
        // Given
        var request = """
            {
                "email": null,
                "name": "Jane Doe",
                "password": "p@$$w0rd",
                "timezone": "UTC"
            }
        """;
        // When
        when(manager.register(any())).thenThrow(new UserMailFailedException("Could not send e-mail", new RuntimeException()));
        // Then
        mockMvc
            .perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.detail").value("Could not send e-mail"));
    }

    @Test
    void register_codeGenerationFailed() throws Exception {
        // Given
        var request = """
            {
                "email": null,
                "name": "Jane Doe",
                "password": "p@$$w0rd",
                "timezone": "UTC"
            }
        """;
        // When
        when(manager.register(any())).thenThrow(new UserCodeGenerationException("Could not generate verification code"));
        // Then
        mockMvc
            .perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.detail").value("Could not generate verification code"));
    }

    @Test
    void register_invalidUser() throws Exception {
        // Given
        var request = """
            {
                "email": "me@example.org",
                "name": "Jane Doe",
                "password": "p@$$w0rd",
                "timezone": "UTC"
            }
        """;
        // When
        when(manager.register(any())).thenThrow(new InvalidUserException("There is already a user with this e-mail address"));
        // Then
        mockMvc
            .perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("There is already a user with this e-mail address"));
    }

    @Test
    void register_constraintViolation() throws Exception {
        // Given
        var request = """
            {
                "email": "me@example.org",
                "name": "Jane Doe",
                "password": "p@$$w0rd",
                "timezone": "UTC"
            }
        """;
        var violation = new MessageConstraintViolation("E-mail is required");
        // When
        when(manager.register(any())).thenThrow(new ConstraintViolationException(Set.of(violation)));
        // Then
        mockMvc
            .perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("E-mail is required"));
    }


    @Test
    void verify_ok() throws Exception {
        // Given
        var verificationCode = "verificationcode";
        var user = Instancio.create(UserDTO.class);
        // When
        when(manager.verify(anyString())).thenReturn(user);
        // Then
        mockMvc
            .perform(post("/api/user/verify")
                .queryParam("code", verificationCode)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.id().toString()));
        verify(manager).verify(verificationCode);
    }

    @Test
    void verify_withoutCsrf() throws Exception {
        // Given
        var verificationCode = "verificationcode";
        // Then
        mockMvc
            .perform(post("/api/user/verify")
                .queryParam("code", verificationCode))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    void verify_invalidUser() throws Exception {
        // Given
        var verificationCode = "doesnotexist";
        // When
        when(manager.verify(anyString())).thenThrow(new InvalidUserException("There is no user with this verification code"));
        // Then
        mockMvc
            .perform(post("/api/user/verify")
                .queryParam("code", verificationCode)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("There is no user with this verification code"));
        verify(manager).verify(verificationCode);
    }

    @Test
    void resetVerification_ok() throws Exception {
        // Given
        var email = "me@example.org";
        // Then
        mockMvc
            .perform(post("/api/user/verify/reset")
                .queryParam("email", email)
                .with(csrf()))
            .andExpect(status().isOk());
        verify(manager).resetVerification(email);
    }

    @Test
    void resetVerification_withoutCsrf() throws Exception {
        // Given
        var email = "me@example.org";
        // Then
        mockMvc
            .perform(post("/api/user/verify/reset")
                .queryParam("email", email))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    void resetVerification_invalidUser() throws Exception {
        // Given
        var email = "me@example.org";
        // When
        doThrow(new InvalidUserException("There is no user with this e-mail address")).when(manager).resetVerification(anyString());
        // Then
        mockMvc
            .perform(post("/api/user/verify/reset")
                .queryParam("email", email)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("There is no user with this e-mail address"));
        verify(manager).resetVerification(email);
    }

    @Test
    void resetVerification_userCodeGenerationFailed() throws Exception {
        // Given
        var email = "me@example.org";
        // When
        doThrow(new UserCodeGenerationException("Could not generate verification code")).when(manager).resetVerification(anyString());
        // Then
        mockMvc
            .perform(post("/api/user/verify/reset")
                .queryParam("email", email)
                .with(csrf()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.detail").value("Could not generate verification code"));
        verify(manager).resetVerification(email);
    }

    @Test
    @WithMockUser
    void findCurrentUser_ok() throws Exception {
        // Given
        var user = Instancio.create(UserDTO.class);
        // When
        when(manager.findCurrentUser()).thenReturn(Optional.of(user));
        // Then
        mockMvc
            .perform(get("/api/user/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.id().toString()));
        verify(manager).findCurrentUser();
    }

    @Test
    void findCurrentUser_unauthorized() throws Exception {
        // Then
        mockMvc
            .perform(get("/api/user/current"))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void findCurrentUser_notFound() throws Exception {
        // Then
        mockMvc
            .perform(get("/api/user/current"))
            .andExpect(status().isNotFound());
        verify(manager).findCurrentUser();
    }

    @Test
    void findAvailableTimezones_ok() throws Exception {
        // Given
        var timezone1 = "Europe/Brussels";
        var timezone2 = "Europe/Paris";
        var search = "euro";
        // When
        when(manager.findAvailableTimezones(any())).thenReturn(List.of(timezone1, timezone2));
        // Then
        mockMvc
            .perform(get("/api/user/timezone")
                .queryParam("search", search))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value(timezone1))
            .andExpect(jsonPath("$[1]").value(timezone2));
        verify(manager).findAvailableTimezones(search);
    }

    @Test
    @WithMockUser
    void update_ok() throws Exception {
        // Given
        var request = """
        {
            "name": "John Doe",
            "timezone": "Europe/Brussels"
        }
        """;
        var user = Instancio.create(UserDTO.class);
        // When
        when(manager.update(any())).thenReturn(user);
        // Then
        mockMvc
            .perform(put("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.id().toString()));
        verify(manager).update(new UpdateUserRequestDTO(
            "John Doe",
            ZoneId.of("Europe/Brussels")
        ));
    }

    @Test
    void update_unauthorized() throws Exception {
        // Given
        var request = """
        {
            "name": "John Doe",
            "timezone": "Europe/Brussels"
        }
        """;
        // Then
        mockMvc
            .perform(put("/api/user")
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
            "name": "John Doe",
            "timezone": "Europe/Brussels"
        }
        """;
        // Then
        mockMvc
            .perform(put("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void update_invalidUser() throws Exception {
        // Given
        var request = """
        {
            "name": "John Doe",
            "timezone": "Europe/Brussels"
        }
        """;
        // When
        when(manager.update(any())).thenThrow(new InvalidUserException("User was not found"));
        // Then
        mockMvc
            .perform(put("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User was not found"));
    }

    @Test
    @WithMockUser
    void updateCredentials_ok() throws Exception {
        // Given
        var request = """
        {
            "oldPassword": "P@$$w0rd",
            "newPassword": "$ecureP@$$w0rd"
        }
        """;
        var user = Instancio.create(UserDTO.class);
        // When
        when(manager.updateCredentials(any())).thenReturn(user);
        // Then
        mockMvc
            .perform(put("/api/user/credentials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.id().toString()));
        verify(manager).updateCredentials(new UpdateCredentialsRequestDTO(
            "P@$$w0rd",
            "$ecureP@$$w0rd"
        ));
    }

    @Test
    void updateCredentials_unauthorized() throws Exception {
        // Given
        var request = """
        {
            "oldPassword": "P@$$w0rd",
            "newPassword": "$ecureP@$$w0rd"
        }
        """;
        // Then
        mockMvc
            .perform(put("/api/user/credentials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void updateCredentials_withoutCsrf() throws Exception {
        // Given
        var request = """
        {
            "oldPassword": "P@$$w0rd",
            "newPassword": "$ecureP@$$w0rd"
        }
        """;
        // Then
        mockMvc
            .perform(put("/api/user/credentials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    @WithMockUser
    void updateCredentials_invalidUser() throws Exception {
        // Given
        var request = """
        {
            "oldPassword": "P@$$w0rd",
            "newPassword": "$ecureP@$$w0rd"
        }
        """;
        // When
        when(manager.updateCredentials(any())).thenThrow(new InvalidUserException("User was not found"));
        // Then
        mockMvc
            .perform(put("/api/user/credentials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("User was not found"));
    }

    @Test
    void requestResetCredentials_ok() throws Exception {
        // Given
        var email = "me@example.org";
        // Then
        mockMvc
            .perform(post("/api/user/credentials/reset/request")
                .queryParam("email", email)
                .with(csrf()))
            .andExpect(status().isOk());
        verify(manager).requestResetCredentials(email);
    }

    @Test
    void requestResetCredentials_withoutCsrf() throws Exception {
        // Given
        var email = "me@example.org";
        // Then
        mockMvc
            .perform(post("/api/user/credentials/reset/request")
                .queryParam("email", email))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    void requestResetCredentials_invalidUser() throws Exception {
        // Given
        var email = "me@example.org";
        // When
        doThrow(new InvalidUserException("There is no user with this e-mail address")).when(manager).requestResetCredentials(anyString());
        // Then
        mockMvc
            .perform(post("/api/user/credentials/reset/request")
                .queryParam("email", email)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("There is no user with this e-mail address"));
        verify(manager).requestResetCredentials(email);
    }

    @Test
    void requestResetCredentials_userCodeGenerationFailed() throws Exception {
        // Given
        var email = "me@example.org";
        // When
        doThrow(new UserCodeGenerationException("Could not generate password reset code")).when(manager).requestResetCredentials(anyString());
        // Then
        mockMvc
            .perform(post("/api/user/credentials/reset/request")
                .queryParam("email", email)
                .with(csrf()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.detail").value("Could not generate password reset code"));
        verify(manager).requestResetCredentials(email);
    }

    @Test
    void confirmResetCredentials_ok() throws Exception {
        // Given
        var request = """
        {
            "passwordResetCode": "code",
            "newPassword": "P@$$w0rd"
        }
        """;
        // Then
        mockMvc
            .perform(post("/api/user/credentials/reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isOk());
        verify(manager).resetCredentials(new ResetCredentialsRequestDTO(
            "code",
            "P@$$w0rd"
        ));
    }

    @Test
    void confirmResetCredentials_withoutCsrf() throws Exception {
        // Given
        var request = """
        {
            "passwordResetCode": "code",
            "newPassword": "P@$$w0rd"
        }
        """;
        // Then
        mockMvc
            .perform(post("/api/user/credentials/reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isForbidden());
        verifyNoInteractions(manager);
    }

    @Test
    void confirmResetCredentials_invalidUser() throws Exception {
        // Given
        var request = """
        {
            "passwordResetCode": "code",
            "newPassword": "P@$$w0rd"
        }
        """;
        // When
        doThrow(new InvalidUserException("Password reset code does not exist")).when(manager).resetCredentials(any());
        // Then
        mockMvc
            .perform(post("/api/user/credentials/reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Password reset code does not exist"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void launchCodeCleanupJob_ok() throws Exception {
        mockMvc
            .perform(post("/api/user/batch/unused-code/start")
                .with(csrf()))
            .andExpect(status().isOk());
        verify(task).run();
    }

    @Test
    void launchCodeCleanupJob_unauthorized() throws Exception {
        mockMvc
            .perform(post("/api/user/batch/unused-code/start")
                .with(csrf()))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(task);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void launchCodeCleanupJob_withoutCsrf() throws Exception {
        mockMvc
            .perform(post("/api/user/batch/unused-code/start"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(task);
    }

    @Test
    @WithMockUser
    void launchCodeCleanupJob_withoutAdminRole() throws Exception {
        mockMvc
            .perform(post("/api/user/batch/unused-code/start")
                .with(csrf()))
            .andExpect(status().isForbidden());
        verifyNoInteractions(task);
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