package codes.dimitri.mediminder.api.assistant.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

@RequiredArgsConstructor
public class RemoveReasoningAdvisor implements CallAdvisor {
    private static final int DEFAULT_ORDER = 0;
    private final int order;

    public RemoveReasoningAdvisor() {
        this(DEFAULT_ORDER);
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientResponse response = chain.nextCall(request);
        ChatResponse chatResponse = response.chatResponse();
        if (chatResponse == null) return response;
        String textResponse = chatResponse.getResult().getOutput().getText();
        String removedReasoning = textResponse.replaceAll("(?s)^.*</think>", "");
        ChatResponse newChatResponse = ChatResponse.builder()
            .from(chatResponse)
            .generations(List.of(new Generation(new AssistantMessage(removedReasoning))))
            .build();
        return new ChatClientResponse(newChatResponse, response.context());
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return order;
    }
}
