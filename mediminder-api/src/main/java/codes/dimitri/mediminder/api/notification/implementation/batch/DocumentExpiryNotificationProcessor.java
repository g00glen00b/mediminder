package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.document.DocumentDTO;
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
class DocumentExpiryNotificationProcessor implements ItemProcessor<DocumentDTO, NotificationEntity> {
    private final NotificationProperties properties;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private final Clock clock;
    private LocalDate today;

    @Override
    public NotificationEntity process(DocumentDTO item) {
        if (!item.expiryDate().isAfter(today)) return createExpiredDocumentNotification(item);
        else return createAlmostExpiredDocumentNotification(item);
    }

    private NotificationEntity createExpiredDocumentNotification(DocumentDTO item) {
        return new NotificationEntity(
            item.userId(),
            NotificationType.DOCUMENT_EXPIRED,
            item.id(),
            "Document expired",
            "Document '" + item.filename() + "' is expired",
            Instant.now(clock).plus(properties.expiry().lifetime())
        );
    }

    private NotificationEntity createAlmostExpiredDocumentNotification(DocumentDTO item) {
        String formattedExpiryDate = FORMATTER.format(item.expiryDate());
        return new NotificationEntity(
            item.userId(),
            NotificationType.DOCUMENT_ALMOST_EXPIRED,
            item.id(),
            "Document almost expired",
            "Document '" + item.filename() + "' will expire on " + formattedExpiryDate,
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
