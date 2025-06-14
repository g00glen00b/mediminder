package codes.dimitri.mediminder.api.assistant.implementation;

import codes.dimitri.mediminder.api.user.UserDTO;
import codes.dimitri.mediminder.api.user.UserManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class UserTool implements AssistantTool {
    private final UserManager manager;

    @Tool(description = "get information of the current user")
    UserDTO findCurrentUser() {
        log.debug("Called findCurrentUser");
        return manager.findCurrentUser();
    }

    @Tool(description = "get current date in ISO format")
    String findCurrentDate() {
        log.debug("Called findCurrentDate");
        UserDTO user = manager.findCurrentUser();
        return manager.calculateTodayForUser(user.id()).toLocalDate().toString();
    }

    @Tool(description = "get current time in ISO format")
    String findCurrentTime() {
        log.debug("Called findCurrentTime");
        UserDTO user = manager.findCurrentUser();
        return manager.calculateTodayForUser(user.id()).toLocalTime().toString();
    }
}
