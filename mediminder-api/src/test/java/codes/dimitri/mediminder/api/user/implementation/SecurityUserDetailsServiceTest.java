package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.SecurityUser;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUserDetailsServiceTest {
    @InjectMocks
    private SecurityUserDetailsService service;
    @Mock
    private UserEntityRepository repository;
    @Spy
    private UserEntityMapper mapper = Mappers.getMapper(UserEntityMapper.class);

    @Nested
    class loadByUsername {
        @Test
        void returnsUser() {
            // Given
            var user = Instancio.create(UserEntity.class);
            // When
            when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            // Then
            UserDetails result = service.loadUserByUsername(user.getEmail());
            assertThat(result).isEqualTo(new SecurityUser(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                user.isAdmin()
            ));
        }

        @Test
        void throwsExceptionIfNotFound() {
            // Given
            var email = "me@example.org";
            // Then
            assertThatExceptionOfType(UsernameNotFoundException.class)
                .isThrownBy(() -> service.loadUserByUsername(email))
                .withMessage("No user found for the given e-mail address");
        }
    }
}