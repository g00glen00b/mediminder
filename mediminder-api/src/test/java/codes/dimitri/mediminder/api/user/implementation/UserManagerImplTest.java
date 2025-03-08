package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ApplicationModuleTest(extraIncludes = "common")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.mail.host=dummy",
    "user.verification-url=http://example.org/user/verify?code=%s",
    "user.password-reset-url=http://example.org/user/confirm-password-reset?code=%s"
})
@Import({
    TestMailSenderConfiguration.class,
    TestClockConfiguration.class
})
@Transactional
@Sql("classpath:test-data/users.sql")
@Sql(value = "classpath:test-data/cleanup-users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserManagerImplTest {
    @Autowired
    private UserManager manager;
    @Autowired
    private MailpitClient mailpitClient;
    @Autowired
    private UserEntityRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
        mailpitClient.deleteAllMessages();
    }

    @Nested
    class findById {
        @Test
        void returnsResult() {
            UUID id = UUID.fromString("03479cd3-7e9a-4b79-8958-522cb1a16b1d");
            UserDTO result = manager.findById(id).orElseThrow();
            assertThat(result).isEqualTo(new UserDTO(
               id,
               "User 1",
               ZoneId.of("UTC"),
               true,
               true
            ));
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.findById(null));
        }
    }

    @Nested
    class findCurrentUser {
        @Test
        @WithUserDetails("me1@example.org")
        void returnsResult() {
            UserDTO result = manager.findCurrentUser().orElseThrow();
            assertThat(result).isEqualTo(new UserDTO(
                UUID.fromString("03479cd3-7e9a-4b79-8958-522cb1a16b1d"),
                "User 1",
                ZoneId.of("UTC"),
                true,
                true
            ));
        }

        @Test
        @WithAnonymousUser
        void returnsNothingIfAnonymous() {
            assertThat(manager.findCurrentUser()).isEmpty();
        }
    }

    @Nested
    class register {
        @Test
        void returnsUser() {
            var request = new RegisterUserRequestDTO(
                "harry.potter@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("UTC")
            );
            UserDTO result = manager.register(request);
            assertThat(result).isEqualTo(new UserDTO(
                result.id(),
                "Harry Potter",
                ZoneId.of("UTC"),
                false,
                false
            ));
        }

        @Test
        void savesEntity() {
            var request = new RegisterUserRequestDTO(
                "harry.potter@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("UTC")
            );
            UserDTO result = manager.register(request);
            UserEntity entity = repository.findById(result.id()).orElseThrow();
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new UserEntity(
                    entity.getId(),
                    "harry.potter@example.org",
                    entity.getPassword(),
                    "Harry Potter",
                    ZoneId.of("UTC"),
                    false,
                    false,
                    entity.getVerificationCode(),
                    null,
                    entity.getLastModifiedDate()
                ));
        }

        @Test
        void hashesPasswordBeforeSavingEntity() {
            var request = new RegisterUserRequestDTO(
                "harry.potter@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("UTC")
            );
            UserDTO result = manager.register(request);
            UserEntity entity = repository.findById(result.id()).orElseThrow();
            boolean matches = passwordEncoder.matches("password", entity.getPassword());
            assertThat(matches).isTrue();
        }

        @Test
        void generatesAVerificationCode() {
            var request = new RegisterUserRequestDTO(
                "harry.potter@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("UTC")
            );
            UserDTO result = manager.register(request);
            UserEntity entity = repository.findById(result.id()).orElseThrow();
            assertThat(entity.getVerificationCode()).isNotBlank();
        }

        @Test
        void sendsVerificationMail() {
            var request = new RegisterUserRequestDTO(
                "harry.potter@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("UTC")
            );
            UserDTO result = manager.register(request);
            UserEntity entity = repository.findById(result.id()).orElseThrow();
            ObjectNode mail = mailpitClient.findFirstMessage();
            assertSoftly(softly -> {
                softly.assertThat(mail.get("From").get("Address").asText()).isEqualTo("noreply@mediminder.org");
                softly.assertThat(mail.get("To").get(0).get("Address").asText()).isEqualTo("harry.potter@example.org");
                softly.assertThat(mail.get("Subject").asText()).isEqualTo("Welcome to Mediminder");
                softly.assertThat(mail.get("Text").asText()).isEqualTo("""
                    Welcome Harry Potter to Mediminder!
                    
                    Before you can use the application, you first need to verify your account by clicking the following link:
                    
                    http://example.org/user/verify?code=%s
                    
                    Be aware, this link is valid for only 24 hours.""".formatted(entity.getVerificationCode()));
            });
        }

        @Test
        void failsIfAlreadyUserWithGivenEmail() {
            var request = new RegisterUserRequestDTO(
                "me1@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("UTC")
            );
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessage("There is already a user with this e-mail address");
        }

        @Test
        void failsIfRequestNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.register(null));
        }

        @Test
        void failsIfEmailNotGiven() {
            var request = new RegisterUserRequestDTO(
                null,
                "password",
                "Harry Potter",
                ZoneId.of("UTC")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessageContaining("E-mail address is required");
        }

        @Test
        void failsIfInvalidEmail() {
            var request = new RegisterUserRequestDTO(
                "foo@",
                "password",
                "Harry Potter",
                ZoneId.of("UTC")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessageContaining("E-mail address 'foo@' is not a valid e-mail address");
        }

        @Test
        void failsIfEmailTooLong() {
            var request = new RegisterUserRequestDTO(
                "a".repeat(129) + "@example.org",
                "password",
                "Harry Potter",
                ZoneId.of("UTC")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessageContaining("E-mail address should not contain more than 128 characters");
        }

        @Test
        void failsIfPasswordNotGiven() {
            var request = new RegisterUserRequestDTO(
                "harry.potter@example.org",
                null,
                "Harry Potter",
                ZoneId.of("UTC")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessageContaining("Password is required");
        }

        @Test
        void failsIfNameTooLong() {
            var request = new RegisterUserRequestDTO(
                "harry.potter@example.org",
                "password",
                "a".repeat(129),
                ZoneId.of("UTC")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessageContaining("Name should not contain more than 128 characters");
        }
    }

    @Nested
    class verify {
        @Test
        void enablesUser() {
            var id = UUID.fromString("44a9dc13-9549-4252-98d1-1bc84e31efcf");
            manager.verify("code3");
            UserEntity entity = repository.findById(id).orElseThrow();
            assertSoftly(softly -> {
                softly.assertThat(entity.isEnabled()).isTrue();
                softly.assertThat(entity.getVerificationCode()).isNull();
            });
        }

        @Test
        void returnsInfo() {
            var id = UUID.fromString("44a9dc13-9549-4252-98d1-1bc84e31efcf");
            UserDTO result = manager.verify("code3");
            assertThat(result).isEqualTo(new UserDTO(
                id,
                "User 4",
                null,
                true,
                false
            ));
        }

        @Test
        void failsIfNoVerificationCodeFound() {
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.verify("unknown"))
                .withMessage("There is no user with this verification code");
        }

        @Test
        void failsIfNoVerificationCodeGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.verify(null));
        }
    }

    @Nested
    class findAvailableTimezones {
        @Test
        void returnsAllJDKSupportedTimezones() {
            Collection<String> results = manager.findAvailableTimezones(null);
            assertThat(results).containsAll(ZoneId.getAvailableZoneIds());
        }

        @Test
        void filtersTimezones() {
            Collection<String> results = manager.findAvailableTimezones("Brussels");
            assertThat(results).containsExactly("Europe/Brussels");
        }
    }

    @Nested
    class resetVerification {
        @Test
        void setsNewVerificationCode() {
            var id = UUID.fromString("44a9dc13-9549-4252-98d1-1bc84e31efcf");
            manager.resetVerification("me3@example.org");
            UserEntity entity = repository.findById(id).orElseThrow();
            assertThat(entity.getVerificationCode())
                .isNotBlank()
                .isNotEqualTo("code3");
        }

        @Test
        void failsIfUserAlreadyEnabled() {
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.resetVerification("me1@example.org"))
                .withMessage("User is already verified");
        }

        @Test
        void failsIfUserNotFound() {
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.resetVerification("doesnotexist@example.org"))
                .withMessage("There is no user found for e-mail address 'doesnotexist@example.org'");
        }

        @Test
        void failsIfInvalidEmailPassed() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.resetVerification("foo@"))
                .withMessageContaining("E-mail must be a valid e-mail address");
        }

        @ParameterizedTest
        @CsvSource(nullValues = "null", value = {
            "'   '",
            "''",
            "null"
        })
        void failsIfNoEmailPassed(String input) {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.resetVerification(input))
                .withMessageContaining("E-mail is required");
        }

        @Test
        void sendsVerificationEmail() {
            var id = UUID.fromString("44a9dc13-9549-4252-98d1-1bc84e31efcf");
            manager.resetVerification("me3@example.org");
            UserEntity entity = repository.findById(id).orElseThrow();
            ObjectNode mail = mailpitClient.findFirstMessage();
            assertSoftly(softly -> {
                softly.assertThat(mail.get("From").get("Address").asText()).isEqualTo("noreply@mediminder.org");
                softly.assertThat(mail.get("To").get(0).get("Address").asText()).isEqualTo("me3@example.org");
                softly.assertThat(mail.get("Subject").asText()).isEqualTo("Welcome to Mediminder");
                softly.assertThat(mail.get("Text").asText()).isEqualTo("""
                    Welcome User 4 to Mediminder!
                    
                    Before you can use the application, you first need to verify your account by clicking the following link:
                    
                    http://example.org/user/verify?code=%s
                    
                    Be aware, this link is valid for only 24 hours.""".formatted(entity.getVerificationCode()));
            });
        }
    }

    @Nested
    class calculateTodayForUser {
        @ParameterizedTest
        @CsvSource({
            "bbca513f-1a16-4233-bbb5-ab4076cca88f,2025-02-26T11:00",
            "0f1f19c2-2d09-43b9-a7fc-ce82b9bfe43a,2025-02-26T00:00",
            "44a9dc13-9549-4252-98d1-1bc84e31efcf,2025-02-26T00:00"
        })
        void returnsCurrentDateForUser(UUID id, LocalDateTime expectedResult) {
            LocalDateTime result = manager.calculateTodayForUser(id);
            assertThat(result).isEqualTo(expectedResult);
        }

        @Test
        void failsIfIdNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.calculateTodayForUser(null));
        }
    }

    @Nested
    class update {
        @Test
        @WithUserDetails("me1@example.org")
        void returnsResult() {
            var request = new UpdateUserRequestDTO(
                "New name",
                ZoneId.of("Europe/Brussels")
            );
            UserDTO result = manager.update(request);
            assertThat(result).isEqualTo(new UserDTO(
               UUID.fromString("03479cd3-7e9a-4b79-8958-522cb1a16b1d"),
               "New name",
                ZoneId.of("Europe/Brussels"),
                true,
                true
            ));
        }

        @Test
        @WithUserDetails("me1@example.org")
        void savesEntity() {
            UUID id = UUID.fromString("03479cd3-7e9a-4b79-8958-522cb1a16b1d");
            var request = new UpdateUserRequestDTO(
                "New name",
                ZoneId.of("Europe/Brussels")
            );
            manager.update(request);
            UserEntity entity = repository.findById(id).orElseThrow();
            assertThat(entity)
                .usingRecursiveComparison()
                .isEqualTo(new UserEntity(
                    id,
                    "me1@example.org",
                    "$2a$10$XaVU/nvIO4RpWzwvB/5Ev..CPrVJeNyumxY3ZJQip4wqdWnf/cbRm",
                    "New name",
                    ZoneId.of("Europe/Brussels"),
                    true,
                    true,
                    null,
                    null,
                    entity.getLastModifiedDate()
                ));
        }

        @Test
        @WithAnonymousUser
        void failsIfUserNotAuthenticated() {
            var request = new UpdateUserRequestDTO(
                "New name",
                ZoneId.of("Europe/Brussels")
            );
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.update(request))
                .withMessage("Could not find user");
        }

        @Test
        void failsIfNameTooLong() {
            var request = new UpdateUserRequestDTO(
                "a".repeat(129),
                ZoneId.of("Europe/Brussels")
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.update(request))
                .withMessageContaining("Name should not contain more than 128 characters");
        }

        @Test
        void failsIfRequestNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.update(null));
        }
    }

    @Nested
    class updateCredentials {
        @Test
        @WithUserDetails("me1@example.org")
        void updatesEntity() {
            UUID id = UUID.fromString("03479cd3-7e9a-4b79-8958-522cb1a16b1d");
            var request = new UpdateCredentialsRequestDTO(
                "password",
                "newpassword"
            );
            manager.updateCredentials(request);
            UserEntity entity = repository.findById(id).orElseThrow();
            assertThat(passwordEncoder.matches("newpassword", entity.getPassword())).isTrue();
        }

        @Test
        @WithUserDetails("me1@example.org")
        void returnsUserInfo() {
            UUID id = UUID.fromString("03479cd3-7e9a-4b79-8958-522cb1a16b1d");
            var request = new UpdateCredentialsRequestDTO(
                "password",
                "newpassword"
            );
            UserDTO result = manager.updateCredentials(request);
            assertThat(result).isEqualTo(new UserDTO(
                id,
                "User 1",
                ZoneId.of("UTC"),
                true,
                true
            ));
        }

        @Test
        @WithUserDetails("me1@example.org")
        void failsIfOldPasswordNotMatching() {
            var request = new UpdateCredentialsRequestDTO(
                "wrongpassword",
                "newpassword"
            );
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessage("Credentials are incorrect");
        }

        @Test
        void failsIfUserNotAuthenticated() {
            var request = new UpdateCredentialsRequestDTO(
                "password",
                "newpassword"
            );
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessage("Could not find user");
        }

        @Test
        void failsIfOldPasswordNotGiven() {
            var request = new UpdateCredentialsRequestDTO(
                null,
                "newpassword"
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessageContaining("Original password is required");
        }

        @Test
        void failsIfNewPasswordNotGiven() {
            var request = new UpdateCredentialsRequestDTO(
                "password",
                null
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessageContaining("New password is required");
        }

        @Test
        void failsIfRequestNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateCredentials(null));
        }
    }

    @Nested
    class requestResetCredentials {
        @Test
        void setsPasswordResetCode() {
            UUID id = UUID.fromString("03479cd3-7e9a-4b79-8958-522cb1a16b1d");
            manager.requestResetCredentials("me1@example.org");
            UserEntity entity = repository.findById(id).orElseThrow();
            assertThat(entity.getPasswordResetCode()).isNotBlank();
        }

        @Test
        void sendsPasswordResetEmail() {
            UUID id = UUID.fromString("03479cd3-7e9a-4b79-8958-522cb1a16b1d");
            manager.requestResetCredentials("me1@example.org");
            ObjectNode mail = mailpitClient.findFirstMessage();
            UserEntity entity = repository.findById(id).orElseThrow();
            assertSoftly(softly -> {
                softly.assertThat(mail.get("From").get("Address").asText()).isEqualTo("noreply@mediminder.org");
                softly.assertThat(mail.get("To").get(0).get("Address").asText()).isEqualTo("me1@example.org");
                softly.assertThat(mail.get("Subject").asText()).isEqualTo("Password reset");
                softly.assertThat(mail.get("Text").asText()).isEqualTo("""
                    Hello!
                    
                    You're receiving this e-mail because someone recently requested to reset the password of the account linked to this e-mail address.
                    
                    If this is you, you can reset your password by clicking the following link:
                    
                    http://example.org/user/confirm-password-reset?code=%s
                    
                    If this wasn't you, you can ignore this e-mail.
                    
                    Be aware, this link is valid for only 24 hours.""".formatted(entity.getPasswordResetCode()));
            });
        }

        @Test
        void failsIfUserNotFound() {
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.requestResetCredentials("doesnotexist@example.org"))
                .withMessage("There is no user found for e-mail address 'doesnotexist@example.org'");
        }

        @ParameterizedTest
        @CsvSource(nullValues = "null", value = {
            "'   '",
            "''",
            "null"
        })
        void failsIfNoEmailPassed(String input) {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.requestResetCredentials(input))
                .withMessageContaining("E-mail is required");
        }

        @Test
        void failsIfInvalidEmailPassed() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.requestResetCredentials("foo@"))
                .withMessageContaining("E-mail must be a valid e-mail address");
        }
    }

    @Nested
    class resetCredentials {
        @Test
        void updatesPassword() {
            var id = UUID.fromString("830257f6-9984-4bb6-9f2d-3710aade0b6a");
            var request = new ResetCredentialsRequestDTO(
                "code1",
                "newpassword"
            );
            manager.resetCredentials(request);
            UserEntity entity = repository.findById(id).orElseThrow();
            assertSoftly(softly -> {
                assertThat(passwordEncoder.matches("newpassword", entity.getPassword())).isTrue();
                assertThat(entity.getPasswordResetCode()).isNull();
            });
        }

        @Test
        void failsIfPasswordResetCodeNotFound() {
            var request = new ResetCredentialsRequestDTO(
                "doesnotexist",
                "newpassword"
            );
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.resetCredentials(request))
                .withMessage("There is no user with this password reset code");
        }

        @Test
        void failsIfPasswordResetCodeMissing() {
            var request = new ResetCredentialsRequestDTO(
                null,
                "newpassword"
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.resetCredentials(request))
                .withMessageContaining("Password reset code is required");
        }

        @Test
        void failsIfNewPasswordMissing() {
            var request = new ResetCredentialsRequestDTO(
                "code1",
                null
            );
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.resetCredentials(request))
                .withMessageContaining("New password is required");
        }

        @Test
        void failsIfRequestNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.resetCredentials(null));
        }
    }
}