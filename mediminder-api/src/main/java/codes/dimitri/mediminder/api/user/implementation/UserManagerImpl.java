package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
        UserEntitySecurityInfo tuple = findOrCreateCurrentUserEntity();
        return mapper.toDTO(tuple.entity(), tuple.securityInfo().authentication().getAuthorities());
    }

    private UserEntitySecurityInfo findOrCreateCurrentUserEntity() {
        return findOAuth2SecurityInfo()
            .map(this::findOrCreate)
            .orElseThrow(CurrentUserNotFoundException::new);
    }

    private UserEntitySecurityInfo findOrCreate(OAuth2SecurityInfo securityInfo) {
        UserEntity entity = repository
            .findById(securityInfo.userId())
            .orElseGet(() -> createEmptyUser(securityInfo.userId()));
        return new UserEntitySecurityInfo(entity, securityInfo);
    }

    private UserEntity createEmptyUser(String id) {
        return repository.save(new UserEntity(id));
    }

    private Optional<OAuth2SecurityInfo> findOAuth2SecurityInfo() {
        return Optional
            .ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .flatMap(this::mapToOAuth2SecurityInfo);
    }

    private Optional<OAuth2SecurityInfo> mapToOAuth2SecurityInfo(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oAuth2User) {
            return Optional.of(new OAuth2SecurityInfo(oAuth2User.getName(), authentication, oAuth2User));
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
        UserEntitySecurityInfo tuple = findOrCreateCurrentUserEntity();
        tuple.entity().setName(request.name());
        tuple.entity().setTimezone(request.timezone());
        return mapper.toDTO(tuple.entity(), tuple.securityInfo().authentication().getAuthorities());
    }

    @Override
    @Transactional
    public void deleteCurrentUser() {
        String userId = findCurrentUserId();
        repository.deleteById(userId);
        eventPublisher.publishEvent(new UserDeletedEvent(userId));
        SecurityContextHolder.clearContext();
    }

    private String findCurrentUserId() {
        return findOAuth2SecurityInfo()
            .map(OAuth2SecurityInfo::userId)
            .orElseThrow(CurrentUserNotFoundException::new);
    }
}
