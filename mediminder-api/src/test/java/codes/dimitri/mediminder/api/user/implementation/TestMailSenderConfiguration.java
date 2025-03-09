package codes.dimitri.mediminder.api.user.implementation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@TestConfiguration
class TestMailSenderConfiguration {
    @Bean
    GenericContainer<?> mailpitContainer() {
        return new GenericContainer<>("axllent/mailpit:latest")
            .withExposedPorts(1025, 8025)
            .waitingFor(Wait.forLogMessage(".*accessible via.*", 1));
    }

    @Bean
    DynamicPropertyRegistrar mailpitRegistrar(GenericContainer<?> mailpitContainer) {
        return registry -> {
            registry.add("spring.mail.host", mailpitContainer::getHost);
            registry.add("spring.mail.port", mailpitContainer::getFirstMappedPort);
            registry.add("mailpit.web.port", () -> mailpitContainer.getMappedPort(8025));
        };
    }

    @Bean
    RestClient mailpitRestClient(RestClient.Builder builder,
                                 @Value("${spring.mail.host}") String host,
                                 @Value("${mailpit.web.port}") int port) {
        return builder.baseUrl("http://" + host + ":" + port + "/api/v1").build();
    }

    @Bean
    MailpitClient mailpitClient(RestClient mailpitRestClient) {
        return new MailpitClient(mailpitRestClient);
    }
}
