package codes.dimitri.mediminder.api.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;

public interface UserManager {
    @Transactional
    UserDTO findCurrentUser();

    Page<UserDTO> findAll(Pageable pageable);

    Collection<String> findAvailableTimezones(String search);

    LocalDateTime calculateTodayForUser(@NotNull String id);

    @Transactional
    UserDTO update(@Valid @NotNull UpdateUserRequestDTO request);

    @Transactional
    void deleteCurrentUser();
}
