package codes.dimitri.mediminder.api.notification.implementation.batch;

import codes.dimitri.mediminder.api.notification.implementation.NotificationEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
@RequiredArgsConstructor
class NotificationCleanupTasklet implements Tasklet {
    private final NotificationEntityRepository repository;
    private final Clock clock;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Instant today = Instant.now(clock);
        repository.deleteAllByDeleteAtBefore(today);
        return RepeatStatus.FINISHED;
    }
}
