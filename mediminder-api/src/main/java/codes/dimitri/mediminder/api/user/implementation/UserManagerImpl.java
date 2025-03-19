package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
class UserManagerImpl implements UserManager {
    private static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("UTC");
    public static final RandomStringUtils RANDOM_STRING_UTILS = RandomStringUtils.secureStrong();
    private final UserEntityRepository repository;
    private final UserEntityMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final UserMailService mailService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Override
    public Optional<UserDTO> findById(@NotNull UUID id) {
        return repository
            .findById(id)
            .map(mapper::toDTO);
    }

    @Override
    public Optional<UserDTO> findCurrentUserOptional() {
        return findCurrentUserId()
            .flatMap(this::findById);
    }

    @Override
    public UserDTO findCurrentUser() {
        return findCurrentUserOptional()
            .orElseThrow(() -> new InvalidUserException("Could not find user"));
    }

    private Optional<UUID> findCurrentUserId() {
        return Optional
            .ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getPrincipal)
            .flatMap(this::findCurrentUserId);
    }

    private Optional<UUID> findCurrentUserId(Object principal) {
        if (principal instanceof SecurityUser securityUser) {
            return Optional.of(securityUser.id());
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public UserDTO register(@Valid @NotNull RegisterUserRequestDTO request) {
        validateUniqueEmail(request.email());
        String hashedPassword = passwordEncoder.encode(request.password());
        String verificationCode = createRandomCode();
        validateUniqueVerificationCode(verificationCode);
        UserEntity entity = repository.save(new UserEntity(request.email(), hashedPassword, request.name(), request.timezone(), verificationCode));
        mailService.sendVerificationMail(entity);
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public UserDTO verify(@NotNull String verificationCode) {
        UserEntity entity = repository
            .findByVerificationCode(verificationCode)
            .orElseThrow(() -> new InvalidUserException("There is no user with this verification code"));
        entity.setEnabled(true);
        entity.setVerificationCode(null);
        return mapper.toDTO(entity);
    }

    private void validateUniqueEmail(String email) {
        if (repository.existsByEmail(email)) throw new InvalidUserException("There is already a user with this e-mail address");
    }

    @Override
    public Collection<String> findAvailableTimezones(String search) {
        return ZoneId
            .getAvailableZoneIds()
            .stream()
            .filter(zoneId -> isZoneIdMatchingSearch(zoneId, search))
            .sorted()
            .toList();
    }

    private static boolean isZoneIdMatchingSearch(String zoneId, String search) {
        return search == null || search.isBlank() || zoneId.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

    @Override
    @Transactional
    public void resetVerification(
        @NotBlank(message = "E-mail is required")
        @Email(message = "E-mail must be a valid e-mail address") String email) {
        UserEntity entity = findEntityByEmail(email);
        if (entity.isEnabled()) throw new InvalidUserException("User is already verified");
        String verificationCode = createRandomCode();
        validateUniqueVerificationCode(verificationCode);
        entity.setVerificationCode(verificationCode);
        mailService.sendVerificationMail(entity);
    }

    private UserEntity findEntityByEmail(String email) {
        return repository
            .findByEmail(email)
            .orElseThrow(() -> new InvalidUserException("There is no user found for e-mail address '" + email + "'"));
    }

    @Override
    public LocalDateTime calculateTodayForUser(@NotNull UUID id) {
        ZoneId timezone = findUserTimezoneOrDummy(id);
        return Instant.now(clock).atZone(timezone).toLocalDateTime();
    }

    private ZoneId findUserTimezoneOrDummy(UUID id) {
        return repository
            .findById(id)
            .map(UserEntity::getTimezone)
            .orElse(DEFAULT_TIMEZONE);
    }

    @Override
    @Transactional
    public UserDTO update(@Valid @NotNull UpdateUserRequestDTO request) {
        UserEntity entity = findCurrentEntity();
        entity.setName(request.name());
        entity.setTimezone(request.timezone());
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public UserDTO updateCredentials(@Valid @NotNull UpdateCredentialsRequestDTO request) {
        UserEntity entity = findCurrentEntity();
        validateCredentials(request, entity);
        String hashedPassword = passwordEncoder.encode(request.newPassword());
        entity.setPassword(hashedPassword);
        return mapper.toDTO(entity);
    }

    private void validateCredentials(UpdateCredentialsRequestDTO request, UserEntity entity) {
        if (!passwordEncoder.matches(request.oldPassword(), entity.getPassword())) {
            throw new InvalidUserException("Credentials are incorrect");
        }
    }

    private UserEntity findCurrentEntity() {
        return findCurrentUserId()
            .flatMap(repository::findById)
            .orElseThrow(() -> new InvalidUserException("Could not find user"));
    }

    @Override
    @Transactional
    public void requestResetCredentials(
        @NotBlank(message = "E-mail is required")
        @Email(message = "E-mail must be a valid e-mail address") String email) {
        UserEntity entity = findEntityByEmail(email);
        String passwordResetCode = createRandomCode();
        validateUniquePasswordResetCode(passwordResetCode);
        entity.setPasswordResetCode(passwordResetCode);
        mailService.sendPasswordResetMail(entity);
    }

    @Override
    @Transactional
    public void resetCredentials(@Valid @NotNull ResetCredentialsRequestDTO request) {
        UserEntity entity = repository
            .findByPasswordResetCode(request.passwordResetCode())
            .orElseThrow(() -> new InvalidUserException("There is no user with this password reset code"));
        entity.setPassword(passwordEncoder.encode(request.newPassword()));
        entity.setPasswordResetCode(null);
    }

    @Override
    public void deleteCurrentUser() {
        UserEntity entity = findCurrentEntity();
        repository.delete(entity);
        eventPublisher.publishEvent(new UserDeletedEvent(entity.getId()));
        SecurityContextHolder.clearContext();
    }

    private void validateUniqueVerificationCode(String verificationCode) {
        if (repository.existsByVerificationCode(verificationCode)) {
            throw new UserCodeGenerationException("Could not generate a unique verification code");
        }
    }

    private void validateUniquePasswordResetCode(String passwordResetCode) {
        if (repository.existsByPasswordResetCode(passwordResetCode)) {
            throw new UserCodeGenerationException("Could not generate a unique password reset code");
        }
    }

    private static String createRandomCode() {
        return RANDOM_STRING_UTILS.nextAlphanumeric(32);
    }
}
