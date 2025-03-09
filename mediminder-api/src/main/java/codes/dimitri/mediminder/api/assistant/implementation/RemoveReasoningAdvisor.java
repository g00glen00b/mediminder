package codes.dimitri.mediminder.api.assistant.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

@RequiredArgsConstructor
public class RemoveReasoningAdvisor implements CallAroundAdvisor {
    private static final int DEFAULT_ORDER = 0;
    private final int order;

    public RemoveReasoningAdvisor() {
        this(DEFAULT_ORDER);
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        AdvisedResponse response = chain.nextAroundCall(request);
        ChatResponse chatResponse = response.response();
        if (chatResponse == null) return response;
        String textResponse = chatResponse.getResult().getOutput().getText();
        String removedReasoning = textResponse.replaceAll("(?s)^.*</think>", "");
        ChatResponse newChatResponse = ChatResponse.builder()
            .from(chatResponse)
            .generations(List.of(new Generation(new AssistantMessage(removedReasoning))))
            .build();
        return new AdvisedResponse(newChatResponse, response.adviseContext());
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
