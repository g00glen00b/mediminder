package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.cabinet.CabinetEntryDTO;
import codes.dimitri.mediminder.api.notification.NotificationType;
import codes.dimitri.mediminder.api.notification.implementation.NotificationEntity;
import codes.dimitri.mediminder.api.notification.implementation.NotificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
@RequiredArgsConstructor
class CabinetEntryExpiryNotificationProcessor implements ItemProcessor<CabinetEntryDTO, NotificationEntity> {
    private final NotificationProperties properties;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private final Clock clock;
    private LocalDate today;

    @Override
    public NotificationEntity process(CabinetEntryDTO item) {
        if (item.medication() == null) return null;
        if (!item.expiryDate().isAfter(today)) return createExpiredCabinetEntryNotification(item);
        else return createAlmostExpiredCabinetEntryNotification(item);
    }

    private NotificationEntity createExpiredCabinetEntryNotification(CabinetEntryDTO item) {
        return new NotificationEntity(
            item.userId(),
            NotificationType.CABINET_ENTRY_EXPIRED,
            item.id(),
            "Cabinet entry expired",
            "A cabinet entry for '" + item.medication().name() + "' is expired",
            Instant.now(clock).plus(properties.expiry().lifetime())
        );
    }

    private NotificationEntity createAlmostExpiredCabinetEntryNotification(CabinetEntryDTO item) {
        String formattedExpiryDate = FORMATTER.format(item.expiryDate());
        return new NotificationEntity(
            item.userId(),
            NotificationType.CABINET_ENTRY_ALMOST_EXPIRED,
            item.id(),
            "Cabinet entry almost expired",
            "A cabinet entry for '" + item.medication().name() + "' will expire on " + formattedExpiryDate,
            Instant.now(clock).plus(properties.expiry().lifetime())
        );
    }

    @BeforeStep
    void initialize(StepExecution stepExecution) {
        LocalDateTime date = stepExecution
            .getJobExecution()
            .getJobParameters()
            .getLocalDateTime("date");
        Objects.requireNonNull(date);
        today = date.toLocalDate();
    }
}
