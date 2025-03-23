package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.document.DocumentDTO;
import codes.dimitri.mediminder.api.document.DocumentManager;
import codes.dimitri.mediminder.api.notification.implementation.NotificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
class DocumentWithNearExpiryDateReader extends AbstractPagingItemReader<DocumentDTO> {
    private final DocumentManager documentManager;
    private final NotificationProperties properties;
    private LocalDate warnDate;

    @Override
    protected void doReadPage() {
        if (results == null) results = new CopyOnWriteArrayList<>();
        else results.clear();
        var pageRequest = PageRequest.of(getPage(), getPageSize());
        Page<DocumentDTO> page = documentManager.findAllWithExpiryDateBefore(warnDate, pageRequest);
        results.addAll(page.getContent());
    }

    @BeforeStep
    void initialize(StepExecution stepExecution) {
        LocalDateTime date = stepExecution
            .getJobExecution()
            .getJobParameters()
            .getLocalDateTime("date");
        Objects.requireNonNull(date);
        warnDate = date.toLocalDate().plus(properties.expiry().warnPeriod());
    }
}
