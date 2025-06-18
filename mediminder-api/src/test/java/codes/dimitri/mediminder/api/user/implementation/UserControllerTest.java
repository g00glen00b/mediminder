package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.UpdateUserRequestDTO;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneId;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private UserManager manager;

    @Nested
    class findCurrentUser {
        @Test
        void returnsResult() throws Exception {
            var user = new UserDTO(
                "auth|ff9d85fcc3c505949092c",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
            );
            when(manager.findCurrentUser()).thenReturn(user);
            mvc
                .perform(get("/api/user/current")
                    .with(oidcLogin())
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.name").value(user.name()))
                .andExpect(jsonPath("$.timezone").value(user.timezone().getId()));
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
                    .with(oidcLogin()))
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
                "auth|ff9d85fcc3c505949092c",
                "Harry Potter",
                ZoneId.of("Europe/Brussels")
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
                    .with(oidcLogin())
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id()));
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
                    .with(oidcLogin())
                    .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid user"))
                .andExpect(jsonPath("$.type").value("https://mediminder/user/invalid"))
                .andExpect(jsonPath("$.detail").value(exception.getMessage()));
        }
    }

    @Nested
    class deleteCurrentUser {
        @Test
        void deletesUser() throws Exception {
            mvc
                .perform(delete("/api/user")
                    .with(oidcLogin())
                    .with(csrf()))
                .andExpect(status().isNoContent());
            verify(manager).deleteCurrentUser();
        }
    }
}