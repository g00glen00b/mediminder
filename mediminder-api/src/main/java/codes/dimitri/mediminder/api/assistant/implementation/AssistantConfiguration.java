package codes.dimitri.mediminder.api.assistant.implementation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
class AssistantConfiguration {
    @Bean
    ChatClient chatClient(
        ChatClient.Builder builder,
        AssistantProperties properties,
        List<? extends AssistantTool> tools) {
        ChatMemory chatMemory = MessageWindowChatMemory
            .builder()
            .maxMessages(10)
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .build();
        return builder
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                new SimpleLoggerAdvisor(),
                new RemoveReasoningAdvisor())
            .defaultSystem(properties.systemTemplate())
            .defaultTools(tools.toArray())
            .build();
    }
}
