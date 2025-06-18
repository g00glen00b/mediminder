package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.assistant.AssistantManager;
import codes.dimitri.mediminder.api.assistant.AssistantRequestDTO;
import codes.dimitri.mediminder.api.assistant.AssistantResponseDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssistantController.class)
class AssistantControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AssistantManager manager;

    @Nested
    class answer {
        @Test
        void returnsResult() throws Exception {
            var request = new AssistantRequestDTO("When do I have to take Dafalgan?");
            var response = new AssistantResponseDTO("You have to take Dafalgan every 8 hours.");
            var json = """
            {
                "question": "When do I have to take Dafalgan?"
            }
            """;
            when(manager.answer(request)).thenReturn(response);
            mvc
                .perform(post("/api/assistant")
                    .contentType("application/json")
                    .content(json)
                    .with(csrf())
                    .with(oauth2Login().authorities(new SimpleGrantedAuthority("Assistant"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("You have to take Dafalgan every 8 hours."));
        }
    }
}