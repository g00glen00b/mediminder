package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.assistant.AssistantManager;
import codes.dimitri.mediminder.api.assistant.AssistantRequestDTO;
import codes.dimitri.mediminder.api.assistant.AssistantResponseDTO;
import codes.dimitri.mediminder.api.assistant.InvalidAssistantException;
import codes.dimitri.mediminder.api.user.CurrentUserNotFoundException;
import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
class AssistantManagerImpl implements AssistantManager {
    private final ChatClient chatClient;
    private final UserManager userManager;

    @Override
    @Retryable(retryFor = MismatchedInputException.class)
    public AssistantResponseDTO answer(@Valid @NotNull AssistantRequestDTO request) {
        UserDTO user = findCurrentUser();
        String content = this.chatClient
            .prompt()
            .advisors(advisors -> advisors.param(ChatMemory.CONVERSATION_ID, user.id()))
            .user(request.question())
            .call()
            .content();
        return new AssistantResponseDTO(content);
    }

    private UserDTO findCurrentUser() {
        try {
            return userManager.findCurrentUser();
        } catch (CurrentUserNotFoundException ex) {
            throw new InvalidAssistantException(ex);
        }
    }
}
