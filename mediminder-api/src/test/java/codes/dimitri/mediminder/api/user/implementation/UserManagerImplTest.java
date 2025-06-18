package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.shared.TestClockConfiguration;
import codes.dimitri.mediminder.api.user.*;
import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.WithOAuth2Login;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ApplicationModuleTest(extraIncludes = "common")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:latest:///mediminder",
    "spring.mail.host=dummy",
    "user.verification-url=http://example.org/user/verify?code=%s",
    "user.password-reset-url=http://example.org/user/confirm-password-reset?code=%s",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2"
})
@Import({
    TestClockConfiguration.class
})
@Transactional
@RecordApplicationEvents
@Sql("classpath:test-data/users.sql")
@Sql(value = "classpath:test-data/cleanup-users.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UserManagerImplTest {
    @Autowired
    private UserManager manager;
    @Autowired
    private UserEntityRepository repository;
    @Autowired
    private ApplicationEvents events;

    @Nested
    class findCurrentUser {
        @Test
        @WithOAuth2Login(claims = @OpenIdClaims(sub = "auth|03479cd37e9a4b798958"))
        void returnsResult() {
            UserDTO result = manager.findCurrentUser();
            assertThat(result).isEqualTo(new UserDTO(
                "auth|03479cd37e9a4b798958",
                "User 1",
                ZoneId.of("UTC")
            ));
        }

        @Test
        @WithAnonymousUser
        void failsIfNotAuthenticated() {
            assertThatExceptionOfType(CurrentUserNotFoundException.class)
                .isThrownBy(() -> manager.findCurrentUser())
                .withMessage("Current user not found");
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
    class calculateTodayForUser {
        @ParameterizedTest
        @CsvSource({
            "auth|bbca513f1a164233bbb5,2025-02-26T21:00",
            "auth|0f1f19c22d0943b9a7fc,2025-02-26T10:00",
            "auth|44a9dc139549425298d1,2025-02-26T10:00"
        })
        void returnsCurrentDateForUser(String id, LocalDateTime expectedResult) {
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
        @WithOAuth2Login(claims = @OpenIdClaims(sub = "auth|03479cd37e9a4b798958"))
        void returnsResult() {
            var request = new UpdateUserRequestDTO(
                "New name",
                ZoneId.of("Europe/Brussels")
            );
            UserDTO result = manager.update(request);
            assertThat(result).isEqualTo(new UserDTO(
               "auth|03479cd37e9a4b798958",
               "New name",
                ZoneId.of("Europe/Brussels")
            ));
        }

        @Test
        @WithOAuth2Login(claims = @OpenIdClaims(sub = "auth|03479cd37e9a4b798958"))
        void savesEntity() {
            String id = "auth|03479cd37e9a4b798958";
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
                    "New name",
                    ZoneId.of("Europe/Brussels"),
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
            assertThatExceptionOfType(CurrentUserNotFoundException.class)
                .isThrownBy(() -> manager.update(request))
                .withMessage("Current user not found");
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
    class delete {
        @Test
        @WithOAuth2Login(claims = @OpenIdClaims(sub = "auth|03479cd37e9a4b798958"))
        void deletesEntity() {
            manager.deleteCurrentUser();
            String userId = "auth|03479cd37e9a4b798958";
            assertThat(repository.existsById(userId)).isFalse();
        }

        @Test
        @WithOAuth2Login(claims = @OpenIdClaims(sub = "auth|03479cd37e9a4b798958"))
        void publishesDeleteEvent() {
            manager.deleteCurrentUser();
            String userId = "auth|03479cd37e9a4b798958";
            assertThat(events.stream(UserDeletedEvent.class)).contains(new UserDeletedEvent(userId));
        }

        @Test
        void failsIfNotAuthenticated() {
            assertThatExceptionOfType(CurrentUserNotFoundException.class)
                .isThrownBy(() -> manager.deleteCurrentUser())
                .withMessage("Current user not found");
        }

    }
}