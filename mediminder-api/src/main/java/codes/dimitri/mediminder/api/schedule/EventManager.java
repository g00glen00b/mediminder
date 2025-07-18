package codes.dimitri.mediminder.api.schedule;

import jakarta.validation.constraints.NotNull;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventManager {
    List<EventDTO> findAll(@NotNull LocalDate targetDate);

    List<EventDTO> findAll(@NotNull LocalDate targetDate, @NotNull String userId);

    @Transactional
    EventDTO complete(@NotNull UUID scheduleId, @NotNull LocalDate targetDate);

    @Transactional
    void uncomplete(@NotNull UUID eventId);
}
