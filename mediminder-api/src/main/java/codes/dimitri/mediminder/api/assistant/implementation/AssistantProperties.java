package codes.dimitri.mediminder.api.assistant.implementation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "assistant")
public record AssistantProperties(
    @DefaultValue("classpath:templates/system.st") Resource systemTemplate,
    @DefaultValue("classpath:templates/user.st") Resource userTemplate,
    @DefaultValue("20") int maxSize
) {
}
