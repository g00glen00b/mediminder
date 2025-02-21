package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.UserMailFailedException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest(classes = {
    UserMailService.class,
    MailSenderAutoConfiguration.class,
    ThymeleafAutoConfiguration.class,
    RestClientAutoConfiguration.class,
    UserMailServiceTest.Configuration.class
}, properties = {
    "spring.mail.username=username",
    "spring.mail.password=password",
    "user.verification-url=http://localhost/verify?code=%s",
    "user.password-reset-url=http://localhost/reset?code=%s"
})
@EnableConfigurationProperties(UserProperties.class)
class UserMailServiceTest {
    @Autowired
    private UserMailService service;
    @Autowired
    private RestClient restClient;
    @SpyBean
    private JavaMailSender mailSender;

    @Container
    static GenericContainer mailhogContainer = new GenericContainer<>("axllent/mailpit:v1.15")
        .withExposedPorts(1025, 8025)
        .waitingFor(Wait.forLogMessage(".*accessible via.*", 1));

    @DynamicPropertySource
    static void configureMail(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", mailhogContainer::getHost);
        registry.add("spring.mail.port", mailhogContainer::getFirstMappedPort);
        registry.add("mailpit.host", mailhogContainer::getHost);
        registry.add("mailpit.port", () -> mailhogContainer.getMappedPort(8025));
    }

    @AfterEach
    void tearDown() {
        deleteAllMessages();
    }

    @Test
    void sendVerificationMail_ok() {
        // Given
        var entity = new UserEntity("me@example.org", "hash", "John Doe", ZoneId.of("UTC"), "verificationcode");
        // When
        service.sendVerificationMail(entity);
        // Then
        ObjectNode result = findFirstMessage();
        assertThat(result.get("From").get("Address").asText()).isEqualTo("noreply@mediminder.org");
        assertThat(result.get("Subject").asText()).isEqualTo("Welcome to Mediminder");
        assertThat(result.get("To").get(0).get("Address").asText()).isEqualTo("me@example.org");
        assertThat(result.get("Text").asText()).isEqualTo("""
            Welcome John Doe to Mediminder!
        
            Before you can use the application, you first need to verify your account by clicking the following link:
        
            http://localhost/verify?code=verificationcode
        
            Be aware, this link is valid for only 24 hours.""");
    }

    @Test
    void sendVerificationMail_mailException() {
        // Given
        var entity = new UserEntity("me@example.org", "hash", "John Doe", ZoneId.of("UTC"), "verificationcode");
        var exception = new MailSendException("Delivery failed");
        // When
        doThrow(exception).when(mailSender).send(ArgumentMatchers.<MimeMessage>any());
        // Then
        assertThatExceptionOfType(UserMailFailedException.class)
            .isThrownBy(() -> service.sendVerificationMail(entity))
            .withMessage("Could not send e-mail to verify user with ID '" + entity.getId() + "'")
            .withCause(exception);
    }

    @Test
    void sendVerificationMail_messagingException() throws MessagingException {
        // Given
        var entity = new UserEntity("me@example.org", "hash", "John Doe", ZoneId.of("UTC"), "verificationcode");
        var mimeMessage = mock(MimeMessage.class);
        var exception = new MessagingException("Could not construct message");
        // When
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(exception).when(mimeMessage).setSubject(anyString());
        // Then
        assertThatExceptionOfType(UserMailFailedException.class)
            .isThrownBy(() -> service.sendVerificationMail(entity))
            .withMessage("Could not send e-mail to verify user with ID '" + entity.getId() + "'")
            .withCause(exception);
    }

    @Test
    void sendPasswordResetMail_ok() {
        // Given
        var entity = new UserEntity("me@example.org", "hash", "John Doe", ZoneId.of("UTC"), null);
        entity.setPasswordResetCode("passwordresetcode");
        // When
        service.sendPasswordResetMail(entity);
        // Then
        ObjectNode result = findFirstMessage();
        assertThat(result.get("From").get("Address").asText()).isEqualTo("noreply@mediminder.org");
        assertThat(result.get("Subject").asText()).isEqualTo("Password reset");
        assertThat(result.get("To").get(0).get("Address").asText()).isEqualTo("me@example.org");
        assertThat(result.get("Text").asText()).isEqualTo("""
            Hello!
            
            You're receiving this e-mail because someone recently requested to reset the password of the account linked to this e-mail address.
            
            If this is you, you can reset your password by clicking the following link:
            
            http://localhost/reset?code=passwordresetcode
            
            If this wasn't you, you can ignore this e-mail.
            
            Be aware, this link is valid for only 24 hours.""");
    }

    @Test
    void sendPasswordResetMail_mailException() {
        // Given
        var entity = new UserEntity("me@example.org", "hash", "John Doe", ZoneId.of("UTC"), "verificationcode");
        var exception = new MailSendException("Delivery failed");
        // When
        doThrow(exception).when(mailSender).send(ArgumentMatchers.<MimeMessage>any());
        // Then
        assertThatExceptionOfType(UserMailFailedException.class)
            .isThrownBy(() -> service.sendPasswordResetMail(entity))
            .withMessage("Could not send e-mail to reset password for user with ID '" + entity.getId() + "'")
            .withCause(exception);
    }

    @Test
    void sendPasswordResetMail_messagingException() throws MessagingException {
        // Given
        var entity = new UserEntity("me@example.org", "hash", "John Doe", ZoneId.of("UTC"), "verificationcode");
        var mimeMessage = mock(MimeMessage.class);
        var exception = new MessagingException("Could not construct message");
        // When
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(exception).when(mimeMessage).setSubject(anyString());
        // Then
        assertThatExceptionOfType(UserMailFailedException.class)
            .isThrownBy(() -> service.sendPasswordResetMail(entity))
            .withMessage("Could not send e-mail to reset password for user with ID '" + entity.getId() + "'")
            .withCause(exception);
    }

    private ObjectNode findFirstMessage() {
        ObjectNode listNode = listAllMessages();
        assertThat(listNode).isNotNull();
        var id = listNode.get("messages").get(0).get("ID").asText();
        ObjectNode messageNode = findMessage(id);
        assertThat(messageNode).isNotNull();
        return messageNode;
    }

    private ObjectNode listAllMessages() {
        return restClient
            .get()
            .uri(builder -> builder.pathSegment("messages").build())
            .retrieve()
            .body(ObjectNode.class);
    }

    private ObjectNode findMessage(String messageId) {
        return restClient
            .get()
            .uri(builder -> builder
                .pathSegment("message", messageId)
                .build())
            .retrieve()
            .body(ObjectNode.class);
    }

    private void deleteAllMessages() {
        restClient
            .delete()
            .uri(builder -> builder.pathSegment("messages").build())
            .retrieve()
            .toBodilessEntity();
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        public RestClient mailpitClient(RestClient.Builder builder, @Value("${mailpit.host}") String host, @Value("${mailpit.port}") int port) {
            return builder
                .baseUrl("http://" + host + ":" + port + "/api/v1")
                .build();
        }
    }

}