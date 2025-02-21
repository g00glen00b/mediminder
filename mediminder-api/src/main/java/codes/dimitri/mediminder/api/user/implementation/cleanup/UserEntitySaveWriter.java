package codes.dimitri.mediminder.api.user.implementation.cleanup;

import codes.dimitri.mediminder.api.user.implementation.UserEntity;
import codes.dimitri.mediminder.api.user.implementation.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class UserEntitySaveWriter implements ItemWriter<UserEntity> {
    private final UserEntityRepository repository;

    @Override
    public void write(Chunk<? extends UserEntity> chunk) {
        repository.saveAll(chunk);
    }
}
