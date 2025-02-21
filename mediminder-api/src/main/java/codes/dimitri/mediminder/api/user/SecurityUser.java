package codes.dimitri.mediminder.api.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record SecurityUser(UUID id, String email, String password, boolean enabled, boolean admin) implements UserDetails {

    private static final SimpleGrantedAuthority USER_AUTHORITY = new SimpleGrantedAuthority("USER");
    private static final SimpleGrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ADMIN");

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        if (admin) return List.of(USER_AUTHORITY, ADMIN_AUTHORITY);
        else return List.of(USER_AUTHORITY);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
