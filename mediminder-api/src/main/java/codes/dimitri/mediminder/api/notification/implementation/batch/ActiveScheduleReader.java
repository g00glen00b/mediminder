package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.notification.implementation.NotificationProperties;
import codes.dimitri.mediminder.api.schedule.ScheduleDTO;
import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.schedule.SchedulePeriodDTO;
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
public class ActiveScheduleReader extends AbstractPagingItemReader<ScheduleDTO> {
    private final ScheduleManager scheduleManager;
    private final NotificationProperties properties;
    private SchedulePeriodDTO period;

    @Override
    protected void doReadPage() {
        if (results == null) results = new CopyOnWriteArrayList<>();
        else results.clear();
        var pageRequest = PageRequest.of(getPage(), getPageSize());
        Page<ScheduleDTO> page = scheduleManager.findAllWithinPeriod(period, pageRequest);
        results.addAll(page.getContent());
    }

    @BeforeStep
    void initialize(StepExecution stepExecution) {
        LocalDateTime date = stepExecution
            .getJobExecution()
            .getJobParameters()
            .getLocalDateTime("date");
        Objects.requireNonNull(date);
        LocalDate start = date.toLocalDate().minus(properties.intake().bufferWindow());
        LocalDate end = date.toLocalDate().plus(properties.intake().bufferWindow());
        period = new SchedulePeriodDTO(start, end);
    }
}
