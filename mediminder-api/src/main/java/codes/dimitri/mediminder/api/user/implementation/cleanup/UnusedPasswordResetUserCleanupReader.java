package codes.dimitri.mediminder.api.user.implementation.cleanup;

import codes.dimitri.mediminder.api.user.implementation.UserEntity;
import codes.dimitri.mediminder.api.user.implementation.UserEntityRepository;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
class UnusedPasswordResetUserCleanupReader extends RepositoryItemReader<UserEntity> {
    private final UserCodeCleanupProperties properties;
    private final Clock clock;

    public UnusedPasswordResetUserCleanupReader(UserEntityRepository repository, UserCodeCleanupProperties properties, Clock clock) {
        this.setRepository(repository);
        this.setMethodName("findAllWithPasswordResetCodeAndLastModifiedBefore");
        this.setSort(Map.of("id", Sort.Direction.ASC));
        this.properties = properties;
        this.clock = clock;
    }

    @BeforeStep
    public void initialize() {
        this.setArguments(List.of(Instant.now(clock).minus(properties.gracePeriod())));
    }

}
