package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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

@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
class UserManagerImpl implements UserManager {
    private static final ZoneId DEFAULT_TIMEZONE = ZoneId.of("UTC");
    private final UserEntityRepository repository;
    private final UserEntityMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Override
    @Transactional
    public UserDTO findCurrentUser() {
        UserEntity entity = findOrCreateCurrentUserEntity();
        return mapper.toDTO(entity);
    }

    @Override
    public Page<UserDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    private UserEntity findOrCreateCurrentUserEntity() {
        return findCurrentUserId()
            .map(this::findOrCreate)
            .orElseThrow(CurrentUserNotFoundException::new);
    }

    private UserEntity findOrCreate(String userId) {
        return repository
            .findById(userId)
            .orElseGet(() -> createEmptyUser(userId));
    }

    private UserEntity createEmptyUser(String id) {
        return repository.save(new UserEntity(id));
    }

    private Optional<String> findCurrentUserId() {
        return Optional
            .ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .flatMap(this::mapToUserid);
    }

    private Optional<String> mapToUserid(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return Optional.of(jwt.getSubject());
        } else {
            return Optional.empty();
        }
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
    public LocalDateTime calculateTodayForUser(@NotNull String id) {
        ZoneId timezone = findUserTimezoneOrDummy(id);
        return Instant.now(clock).atZone(timezone).toLocalDateTime();
    }

    private ZoneId findUserTimezoneOrDummy(String id) {
        return repository
            .findById(id)
            .map(UserEntity::getTimezone)
            .orElse(DEFAULT_TIMEZONE);
    }

    @Override
    @Transactional
    public UserDTO update(@Valid @NotNull UpdateUserRequestDTO request) {
        UserEntity entity = findOrCreateCurrentUserEntity();
        entity.setName(request.name());
        entity.setTimezone(request.timezone());
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional
    public void deleteCurrentUser() {
        String userId = findCurrentUserId().orElseThrow(CurrentUserNotFoundException::new);
        repository.deleteById(userId);
        eventPublisher.publishEvent(new UserDeletedEvent(userId));
        SecurityContextHolder.clearContext();
    }
}
