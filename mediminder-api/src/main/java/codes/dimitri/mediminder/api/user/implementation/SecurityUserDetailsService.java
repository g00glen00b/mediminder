package codes.dimitri.mediminder.api.user.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class SecurityUserDetailsService implements UserDetailsService {
    private final UserEntityRepository repository;
    private final UserEntityMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository
            .findByEmail(email)
            .map(mapper::toSecurityUser)
            .orElseThrow(() -> new UsernameNotFoundException("No user found for the given e-mail address"));
    }
}
