package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.*;
import jakarta.mail.MessagingException;
import jakarta.validation.ConstraintViolationException;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UserManagerImpl.class, UserEntityMapperImpl.class, UserManagerImplTest.Configuration.class})
@ContextConfiguration(classes = ValidationAutoConfiguration.class)
class UserManagerImplTest {
    private static final ZonedDateTime TODAY = ZonedDateTime.of(2024, 3, 20, 10, 0, 0, 0, ZoneId.of("UTC"));
    @Autowired
    private UserManagerImpl manager;
    @MockBean
    private UserEntityRepository repository;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private UserMailService mailService;
    @Captor
    private ArgumentCaptor<UserEntity> anyEntity;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class findById {
        @Test
        void returnsDTO() {
            // Given
            var user = Instancio.create(UserEntity.class);
            // When
            when(repository.findById(any())).thenReturn(Optional.of(user));
            // Then
            Optional<UserDTO> result = manager.findById(user.getId());
            assertThat(result).contains(new UserDTO(
                user.getId(),
                user.getName(),
                user.getTimezone(),
                user.isEnabled(),
                user.isAdmin()
            ));
            verify(repository).findById(user.getId());
        }

        @Test
        void returnsNothing() {
            // Given
            var id = UUID.randomUUID();
            // Then
            Optional<UserDTO> result = manager.findById(id);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class findCurrentUser {
        @Test
        void returnsDTO() {
            // Given
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            when(repository.findById(any())).thenReturn(Optional.of(entity));
            SecurityContextHolder.setContext(securityContext);
            // Then
            Optional<UserDTO> result = manager.findCurrentUser();
            assertThat(result).contains(new UserDTO(
                entity.getId(),
                entity.getName(),
                entity.getTimezone(),
                entity.isEnabled(),
                entity.isAdmin()
            ));
            verify(repository).findById(entity.getId());
        }

        @Test
        void returnsNothingIfUserNotFound() {
            // Given
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            // Then
            Optional<UserDTO> result = manager.findCurrentUser();
            assertThat(result).isEmpty();
            verify(repository).findById(entity.getId());
        }

        @Test
        void returnsNothingIfWrongPrincipal() {
            // Given
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(new Object(), securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            // Then
            Optional<UserDTO> result = manager.findCurrentUser();
            assertThat(result).isEmpty();
            verifyNoInteractions(repository);
        }

        @Test
        void returnsNothingIfNoPrincipal() {
            // Given
            var securityContext = new SecurityContextImpl(null);
            // When
            SecurityContextHolder.setContext(securityContext);
            // Then
            Optional<UserDTO> result = manager.findCurrentUser();
            assertThat(result).isEmpty();
            verifyNoInteractions(repository);
        }

        @Test
        void returnsNothingIfNoSecurityContext() {
            Optional<UserDTO> result = manager.findCurrentUser();
            assertThat(result).isEmpty();
            verifyNoInteractions(repository);
        }
    }

    @Nested
    class register {
        @Test
        void returnsDTO() {
            // Given
            var request = Instancio.of(RegisterUserRequestDTO.class)
                .generate(field(RegisterUserRequestDTO::email), gen -> gen.net().email())
                .create();
            // When
            when(repository.save(any())).thenAnswer(returnsFirstArg());
            when(passwordEncoder.encode(anyString())).thenAnswer(args -> "hash" + args.getArgument(0));
            // Then
            UserDTO result = manager.register(request);
            verify(repository).save(anyEntity.capture());
            assertThat(result).isEqualTo(new UserDTO(
                anyEntity.getValue().getId(),
                request.name(),
                request.timezone(),
                false,
                false
            ));
        }

        @Test
        void savesEntity() {
            // Given
            var request = Instancio.of(RegisterUserRequestDTO.class)
                .generate(field(RegisterUserRequestDTO::email), gen -> gen.net().email())
                .create();
            // When
            when(repository.save(any())).thenAnswer(returnsFirstArg());
            when(passwordEncoder.encode(anyString())).thenAnswer(args -> "hash" + args.getArgument(0));
            // Then
            manager.register(request);
            verify(repository).save(anyEntity.capture());
            verify(passwordEncoder).encode(request.password());
            verify(repository).existsByVerificationCode(anyEntity.getValue().getVerificationCode());
            verify(repository).existsByEmail(request.email());
            verify(mailService).sendVerificationMail(anyEntity.getValue());
            assertThat(anyEntity.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id", "verificationCode")
                .isEqualTo(new UserEntity(
                    request.email(),
                    "hash" + request.password(),
                    request.name(),
                    request.timezone(),
                    null
                ));
            assertThat(anyEntity.getValue().getVerificationCode()).isNotNull();
        }

        @Test
        void throwsExceptionIfAlreadyUserWithEmail() {
            // Given
            var request = Instancio.of(RegisterUserRequestDTO.class)
                .generate(field(RegisterUserRequestDTO::email), gen -> gen.net().email())
                .create();
            // When
            when(repository.existsByEmail(anyString())).thenReturn(true);
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessage("There is already a user with this e-mail address");
            verify(repository, never()).save(any());
        }

        @Test
        void throwsExceptionIfAlreadyUserWithVerificationCode() {
            // Given
            var request = Instancio.of(RegisterUserRequestDTO.class)
                .generate(field(RegisterUserRequestDTO::email), gen -> gen.net().email())
                .create();
            // When
            when(repository.existsByVerificationCode(anyString())).thenReturn(true);
            // Then
            assertThatExceptionOfType(UserCodeGenerationException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessage("Could not generate a unique verification code");
            verify(repository, never()).save(any());
        }

        @Test
        void throwsExceptionIfSendingMailFails() {
            // Given
            var request = Instancio.of(RegisterUserRequestDTO.class)
                .generate(field(RegisterUserRequestDTO::email), gen -> gen.net().email())
                .create();
            // When
            doThrow(new UserMailFailedException("Could not send e-mail", new MessagingException("Error"))).when(mailService).sendVerificationMail(any());
            // Then
            assertThatExceptionOfType(UserMailFailedException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessage("Could not send e-mail");
        }

        @Test
        void throwsExceptionIfNoEmailGiven() {
            // Given
            var request = Instancio.of(RegisterUserRequestDTO.class)
                .ignore(field(RegisterUserRequestDTO::email))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessageEndingWith("E-mail address is required");
        }

        @Test
        void throwsExceptionIfNotValidEmailGiven() {
            // Given
            var request = Instancio.of(RegisterUserRequestDTO.class)
                .set(field(RegisterUserRequestDTO::email), "notavalidemail")
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessageEndingWith("E-mail address 'notavalidemail' is not a valid e-mail address");
        }

        @Test
        void throwsExceptionIfNoPasswordGiven() {
            // Given
            var request = Instancio.of(RegisterUserRequestDTO.class)
                .generate(field(RegisterUserRequestDTO::email), gen -> gen.net().email())
                .ignore(field(RegisterUserRequestDTO::password))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessageEndingWith("Password is required");
        }

        @Test
        void throwsExceptionIfNameTooLong() {
            // Given
            var request = Instancio.of(RegisterUserRequestDTO.class)
                .generate(field(RegisterUserRequestDTO::email), gen -> gen.net().email())
                .generate(field(RegisterUserRequestDTO::name), gen -> gen.string().length(129))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.register(request))
                .withMessageEndingWith("Name should not contain more than 128 characters");
        }
    }

    @Nested
    class verify {
        @Test
        void returnsDTO() {
            // Given
            var verificationCode = "verificationCode";
            var user = Instancio.of(UserEntity.class)
                .ignore(field(UserEntity::isEnabled))
                .set(field(UserEntity::getVerificationCode), verificationCode)
                .create();
            // When
            when(repository.findByVerificationCode(anyString())).thenReturn(Optional.of(user));
            // Then
            UserDTO result = manager.verify(user.getVerificationCode());
            assertThat(result).isEqualTo(new UserDTO(
                user.getId(),
                user.getName(),
                user.getTimezone(),
                user.isEnabled(),
                user.isAdmin()
            ));
            verify(repository).findByVerificationCode(verificationCode);
        }

        @Test
        void updatesEntity() {
            // Given
            var user = Instancio.of(UserEntity.class)
                .ignore(field(UserEntity::isEnabled))
                .create();
            // When
            when(repository.findByVerificationCode(anyString())).thenReturn(Optional.of(user));
            // Then
            manager.verify(user.getVerificationCode());
            assertThat(user.isEnabled()).isTrue();
            assertThat(user.getVerificationCode()).isNull();
        }

        @Test
        void throwsExceptionIfNoUserFound() {
            // Given
            var code = "doesnotexist";
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.verify(code))
                .withMessage("There is no user with this verification code");
        }
    }

    @ParameterizedTest
    @CsvSource(value = {
        "bru,2",
        "brus,1",
        "' ',603",
        "'',603",
        "null,603"
    }, nullValues = "null")
    void findAvailableTimzones(String search, int expectedResults) {
        Collection<String> results = manager.findAvailableTimezones(search);
        assertThat(results).hasSize(expectedResults);
    }

    @Nested
    class resetVerification {
        @Test
        void resetsVerificationCode() {
            // Given
            var verificationCode = "verificationCode";
            var user = Instancio.of(UserEntity.class)
                .ignore(field(UserEntity::isEnabled))
                .generate(field(UserEntity::getEmail), gen -> gen.net().email())
                .set(field(UserEntity::getVerificationCode), verificationCode)
                .create();
            // When
            when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
            // Then
            manager.resetVerification(user.getEmail());
            assertThat(user.getVerificationCode()).isNotEqualTo(verificationCode);
            verify(repository).findByEmail(user.getEmail());
            verify(repository).existsByVerificationCode(user.getVerificationCode());
            verify(mailService).sendVerificationMail(user);
        }

        @Test
        void throwsExceptionIfUserNotFound() {
            // Given
            var verificationCode = "verificationCode";
            var user = Instancio.of(UserEntity.class)
                .ignore(field(UserEntity::isEnabled))
                .generate(field(UserEntity::getEmail), gen -> gen.net().email())
                .set(field(UserEntity::getVerificationCode), verificationCode)
                .create();
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.resetVerification(user.getEmail()))
                .withMessage("There is no user found for e-mail address '" + user.getEmail() + "'");
        }

        @Test
        void throwsExceptionIfUserAlreadyEnabled() {
            // Given
            var user = Instancio.of(UserEntity.class)
                .set(field(UserEntity::isEnabled), true)
                .generate(field(UserEntity::getEmail), gen -> gen.net().email())
                .ignore(field(UserEntity::getVerificationCode))
                .create();
            // When
            when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.resetVerification(user.getEmail()))
                .withMessage("User is already verified");
        }

        @Test
        void throwsExceptionIfVerificationCodeCouldNotBeGenerated() {
            // Given
            var verificationCode = "verificationCode";
            var user = Instancio.of(UserEntity.class)
                .ignore(field(UserEntity::isEnabled))
                .generate(field(UserEntity::getEmail), gen -> gen.net().email())
                .set(field(UserEntity::getVerificationCode), verificationCode)
                .create();
            // When
            when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
            when(repository.existsByVerificationCode(anyString())).thenReturn(true);
            // Then
            assertThatExceptionOfType(UserCodeGenerationException.class)
                .isThrownBy(() -> manager.resetVerification(user.getEmail()))
                .withMessage("Could not generate a unique verification code");
        }

        @Test
        void throwsExceptionIfMailCouldNotBeSent() {
            // Given
            var verificationCode = "verificationCode";
            var user = Instancio.of(UserEntity.class)
                .ignore(field(UserEntity::isEnabled))
                .generate(field(UserEntity::getEmail), gen -> gen.net().email())
                .set(field(UserEntity::getVerificationCode), verificationCode)
                .create();
            var exception = new UserMailFailedException("Mail delivery failed", new MessagingException("Error"));
            // When
            when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));
            doThrow(exception).when(mailService).sendVerificationMail(any());
            // Then
            assertThatExceptionOfType(UserMailFailedException.class)
                .isThrownBy(() -> manager.resetVerification(user.getEmail()))
                .withMessage("Mail delivery failed");
        }

        @Test
        void throwsExceptionIfNotAValidEmail() {
            // Given
            var email = "notavalidemailaddress";
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.resetVerification(email))
                .withMessageEndingWith("E-mail must be a valid e-mail address");
        }

        @Test
        void throwsExceptionIfNoEmailGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.resetVerification(null))
                .withMessageEndingWith("E-mail is required");
        }
    }

    @Nested
    class calculateTodayForUser {
        @Test
        void returnsDate() {
            // Given
            var user = Instancio.of(UserEntity.class)
                .set(field(UserEntity::getTimezone), ZoneId.of("Europe/Brussels"))
                .create();
            // When
            when(repository.findById(any())).thenReturn(Optional.of(user));
            // Then
            LocalDateTime result = manager.calculateTodayForUser(user.getId());
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 3, 20, 11, 0, 0));
            verify(repository).findById(user.getId());
        }

        @Test
        void returnsDummyDateIfUserHasNoTimezone() {
            // Given
            var user = Instancio.of(UserEntity.class)
                .ignore(field(UserEntity::getTimezone))
                .create();
            // When
            when(repository.findById(any())).thenReturn(Optional.of(user));
            // Then
            LocalDateTime result = manager.calculateTodayForUser(user.getId());
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 3, 20, 10, 0, 0));
            verify(repository).findById(user.getId());
        }

        @Test
        void returnsDummyTimezoneIfNoUserFound() {
            // Given
            var id = UUID.randomUUID();
            // Then
            LocalDateTime result = manager.calculateTodayForUser(id);
            assertThat(result).isEqualTo(LocalDateTime.of(2024, 3, 20, 10, 0, 0));
            verify(repository).findById(id);
        }
    }

    @Nested
    class update {
        @Test
        void returnsDTO() {
            // Given
            var request = Instancio.create(UpdateUserRequestDTO.class);
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            when(repository.findById(any())).thenReturn(Optional.of(entity));
            // Then
            UserDTO result = manager.update(request);
            assertThat(result).isEqualTo(new UserDTO(
                entity.getId(),
                request.name(),
                request.timezone(),
                entity.isEnabled(),
                entity.isAdmin()
            ));
        }

        @Test
        void updatesEntity() {
            // Given
            var request = Instancio.create(UpdateUserRequestDTO.class);
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            when(repository.findById(any())).thenReturn(Optional.of(entity));
            // Then
            manager.update(request);
            assertThat(entity).usingRecursiveComparison().isEqualTo(new UserEntity(
               entity.getId(),
               entity.getEmail(),
               entity.getPassword(),
               request.name(),
               request.timezone(),
               entity.isEnabled(),
               entity.isAdmin(),
               entity.getVerificationCode(),
               entity.getPasswordResetCode(),
               entity.getLastModifiedDate()
            ));
            verify(repository).findById(entity.getId());
        }

        @Test
        void throwsExceptionIfUserNotFound() {
            // Given
            var request = Instancio.create(UpdateUserRequestDTO.class);
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.update(request))
                .withMessage("Could not find user");
        }

        @Test
        void throwsExceptionIfWrongPrincipal() {
            // Given
            var request = Instancio.create(UpdateUserRequestDTO.class);
            var authentication = new UsernamePasswordAuthenticationToken(new Object(), null);
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.update(request))
                .withMessage("Could not find user");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfNoAuthentication() {
            // Given
            var request = Instancio.create(UpdateUserRequestDTO.class);
            var securityContext = new SecurityContextImpl(null);
            // When
            SecurityContextHolder.setContext(securityContext);
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.update(request))
                .withMessage("Could not find user");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfEmptySecurityContext() {
            // Given
            var request = Instancio.create(UpdateUserRequestDTO.class);
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.update(request))
                .withMessage("Could not find user");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfNameTooLong() {
            // Given
            var request = Instancio.of(UpdateUserRequestDTO.class)
                .generate(field(UpdateUserRequestDTO::name), gen -> gen.string().length(129))
                .create();
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.update(request))
                .withMessageEndingWith("Name should not contain more than 128 characters");
            verifyNoInteractions(repository);
        }
    }

    @Nested
    class updateCredentials {
        @Test
        void returnsDTO() {
            // Given
            var request = Instancio.create(UpdateCredentialsRequestDTO.class);
            var entity = Instancio.of(UserEntity.class)
                .set(field(UserEntity::getPassword), request.oldPassword())
                .create();
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            when(repository.findById(any())).thenReturn(Optional.of(entity));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(passwordEncoder.encode(anyString())).thenAnswer(args -> "hash" + args.getArgument(0));
            // Then
            UserDTO result = manager.updateCredentials(request);
            assertThat(result).isEqualTo(new UserDTO(
                entity.getId(),
                entity.getName(),
                entity.getTimezone(),
                entity.isEnabled(),
                entity.isAdmin()
            ));
        }

        @Test
        void updatesEntity() {
            // Given
            var request = Instancio.create(UpdateCredentialsRequestDTO.class);
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            when(repository.findById(any())).thenReturn(Optional.of(entity));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(passwordEncoder.encode(anyString())).thenAnswer(args -> "hash" + args.getArgument(0));
            // Then
            manager.updateCredentials(request);
            assertThat(entity).usingRecursiveComparison().isEqualTo(new UserEntity(
                entity.getId(),
                entity.getEmail(),
                "hash" + request.newPassword(),
                entity.getName(),
                entity.getTimezone(),
                entity.isEnabled(),
                entity.isAdmin(),
                entity.getVerificationCode(),
                entity.getPasswordResetCode(),
                entity.getLastModifiedDate()
            ));
            verify(repository).findById(entity.getId());
            verify(passwordEncoder).encode(request.newPassword());
            verify(passwordEncoder).matches(request.oldPassword(), securityUser.getPassword());
        }

        @Test
        void throwsExceptionIfUserNotFound() {
            // Given
            var request = Instancio.create(UpdateCredentialsRequestDTO.class);
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessage("Could not find user");
        }

        @Test
        void throwsExceptionIfWrongPrincipal() {
            // Given
            var request = Instancio.create(UpdateCredentialsRequestDTO.class);
            var authentication = new UsernamePasswordAuthenticationToken(new Object(), null);
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessage("Could not find user");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfNoAuthentication() {
            // Given
            var request = Instancio.create(UpdateCredentialsRequestDTO.class);
            var securityContext = new SecurityContextImpl(null);
            // When
            SecurityContextHolder.setContext(securityContext);
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessage("Could not find user");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfEmptySecurityContext() {
            // Given
            var request = Instancio.create(UpdateCredentialsRequestDTO.class);
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessage("Could not find user");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfOldPasswordNotGiven() {
            // Given
            var request = Instancio.of(UpdateCredentialsRequestDTO.class)
                .ignore(field(UpdateCredentialsRequestDTO::oldPassword))
                .create();
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);

            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessageEndingWith("Original password is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfNewPasswordNotGiven() {
            // Given
            var request = Instancio.of(UpdateCredentialsRequestDTO.class)
                .ignore(field(UpdateCredentialsRequestDTO::newPassword))
                .create();
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);

            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessageEndingWith("New password is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfOldPasswordNotMatching() {
            // Given
            var request = Instancio.create(UpdateCredentialsRequestDTO.class);
            var entity = Instancio.create(UserEntity.class);
            var securityUser = new SecurityUser(entity.getId(), entity.getEmail(), entity.getPassword(), entity.isEnabled(), entity.isAdmin());
            var authentication = new UsernamePasswordAuthenticationToken(securityUser, securityUser.getPassword());
            var securityContext = new SecurityContextImpl(authentication);
            // When
            SecurityContextHolder.setContext(securityContext);
            when(repository.findById(any())).thenReturn(Optional.of(entity));
            when(passwordEncoder.encode(anyString())).thenAnswer(args -> "hash" + args.getArgument(0));
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.updateCredentials(request))
                .withMessage("Credentials are incorrect");
        }
    }

    @Nested
    class requestResetCredentials {
        @Test
        void setsResetCode() {
            // Given
            var entity = Instancio.of(UserEntity.class)
                .ignore(field(UserEntity::getPasswordResetCode))
                .generate(field(UserEntity::getEmail), gen -> gen.net().email())
                .create();
            // When
            when(repository.findByEmail(any())).thenReturn(Optional.of(entity));
            // Then
            manager.requestResetCredentials(entity.getEmail());
            assertThat(entity.getPasswordResetCode()).isNotNull();
            verify(mailService).sendPasswordResetMail(entity);
            verify(repository).findByEmail(entity.getEmail());
            verify(repository).existsByPasswordResetCode(entity.getPasswordResetCode());
        }

        @Test
        void throwsExceptionIfUserNotFound() {
            // Given
            var entity = Instancio.of(UserEntity.class)
                .ignore(field(UserEntity::getPasswordResetCode))
                .generate(field(UserEntity::getEmail), gen -> gen.net().email())
                .create();
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.requestResetCredentials(entity.getEmail()))
                .withMessage("There is no user found for e-mail address '" + entity.getEmail() + "'");
        }

        @Test
        void throwsExceptionIfUnableToGenerateUniqueCode() {
            // Given
            var entity = Instancio.of(UserEntity.class)
                .ignore(field(UserEntity::getPasswordResetCode))
                .generate(field(UserEntity::getEmail), gen -> gen.net().email())
                .create();
            // When
            when(repository.findByEmail(any())).thenReturn(Optional.of(entity));
            when(repository.existsByPasswordResetCode(anyString())).thenReturn(true);
            // Then
            assertThatExceptionOfType(UserCodeGenerationException.class)
                .isThrownBy(() -> manager.requestResetCredentials(entity.getEmail()))
                .withMessage("Could not generate a unique password reset code");
        }

        @Test
        void throwsExceptionIfEmailNotGiven() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.requestResetCredentials(null));
        }

        @Test
        void throwsExceptionIfEmailNotValid() {
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.requestResetCredentials("invalidemail"));
        }
    }

    @Nested
    class resetCredentials {
        @Test
        void updatesEntity() {
            // Given
            var entity = Instancio.create(UserEntity.class);
            var request = Instancio.of(ResetCredentialsRequestDTO.class)
                .set(field(ResetCredentialsRequestDTO::passwordResetCode), entity.getPasswordResetCode())
                .create();
            // When
            when(repository.findByPasswordResetCode(anyString())).thenReturn(Optional.of(entity));
            when(passwordEncoder.encode(anyString())).thenAnswer(args -> "hash" + args.getArgument(0));
            // Then
            manager.resetCredentials(request);
            verify(repository).findByPasswordResetCode(request.passwordResetCode());
            assertThat(entity.getPasswordResetCode()).isNull();
            assertThat(entity.getPassword()).isEqualTo("hash" + request.newPassword());
        }

        @Test
        void throwsExceptionIfUserNotFound() {
            // Given
            var request = Instancio.create(ResetCredentialsRequestDTO.class);
            // Then
            assertThatExceptionOfType(InvalidUserException.class)
                .isThrownBy(() -> manager.resetCredentials(request))
                .withMessage("There is no user with this password reset code");
        }

        @Test
        void throwsExceptionIfPasswordResetCodeNotGiven() {
            // Given
            var request = Instancio.of(ResetCredentialsRequestDTO.class)
                .ignore(field(ResetCredentialsRequestDTO::passwordResetCode))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.resetCredentials(request))
                .withMessageEndingWith("Password reset code is required");
            verifyNoInteractions(repository);
        }

        @Test
        void throwsExceptionIfNewPasswordNotGiven() {
            // Given
            var request = Instancio.of(ResetCredentialsRequestDTO.class)
                .ignore(field(ResetCredentialsRequestDTO::newPassword))
                .create();
            // Then
            assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> manager.resetCredentials(request))
                .withMessageEndingWith("New password is required");
            verifyNoInteractions(repository);
        }
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        public Clock clock() {
            return Clock.fixed(TODAY.toInstant(), TODAY.getZone());
        }
    }
}