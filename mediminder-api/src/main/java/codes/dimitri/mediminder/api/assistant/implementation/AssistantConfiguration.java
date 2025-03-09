package codes.dimitri.mediminder.api.assistant.implementation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssistantConfiguration {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, AssistantProperties properties) {
        var memory = new InMemoryChatMemory();
        return builder
            .defaultAdvisors(
                new PromptChatMemoryAdvisor(memory),
                new SimpleLoggerAdvisor(),
                new RemoveReasoningAdvisor())
            .defaultSystem(properties.systemTemplate())
            .defaultUser(properties.userTemplate())
            .build();
    }
}
