package codes.dimitri.mediminder.api.assistant.implementation;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

@TestConfiguration
class TestOpenAIConfiguration {
    @Bean
    @RestartScope
    GenericContainer<?> openAiContainer() {
        return new GenericContainer<>("wiremock/wiremock:3.12.1")
            .withExposedPorts(8080)
            .withCopyFileToContainer(
                MountableFile.forClasspathResource("wiremock/mappings"),
                "/home/wiremock/mappings")
            .withCommand("--disable-http2-plain")
            .withReuse(true);
    }

    @Bean
    DynamicPropertyRegistrar openAiProperties(GenericContainer<?> openAiContainer) {
        return registry -> {
            registry.add("spring.ai.openai.base-url", () -> "http://" + openAiContainer.getHost() + ":" + openAiContainer.getFirstMappedPort());
        };
    }
}
