package codes.dimitri.mediminder.api.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUserTest {
    @Nested
    class getAuthorities {
        @Test
        void returnsUserRole() {
            // Given
            var user = new SecurityUser(UUID.randomUUID(), "me@example.org", "password", true, false);
            // Then
            assertThat(user.getAuthorities()).containsOnly(new SimpleGrantedAuthority("USER"));
        }

        @Test
        void returnsUserAndAdminRoleForAdmins() {
            // Given
            var user = new SecurityUser(UUID.randomUUID(), "me@example.org", "password", true, true);
            // Then
            assertThat(user.getAuthorities()).containsOnly(
                new SimpleGrantedAuthority("USER"),
                new SimpleGrantedAuthority("ADMIN"));
        }
    }
}