package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.schedule.ScheduleManager;
import codes.dimitri.mediminder.api.schedule.UserScheduledMedicationDTO;
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
class UserScheduledMedicationReader extends AbstractPagingItemReader<UserScheduledMedicationDTO> {
    private final ScheduleManager scheduleManager;
    private LocalDate today;

    @Override
    protected void doReadPage() {
        if (results == null) results = new CopyOnWriteArrayList<>();
        else results.clear();
        var pageRequest = PageRequest.of(getPage(), getPageSize());
        Page<UserScheduledMedicationDTO> page = scheduleManager.findAllUserScheduledMedicationOnDate(today, pageRequest);
        results.addAll(page.getContent());
    }

    @BeforeStep
    void initialize(StepExecution stepExecution) {
        LocalDateTime date = stepExecution
            .getJobExecution()
            .getJobParameters()
            .getLocalDateTime("date");
        Objects.requireNonNull(date);
        this.today = date.toLocalDate();
    }
}
