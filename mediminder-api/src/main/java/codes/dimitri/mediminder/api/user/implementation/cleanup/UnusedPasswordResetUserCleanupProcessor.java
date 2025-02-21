package codes.dimitri.mediminder.api.user.implementation.cleanup;

import codes.dimitri.mediminder.api.user.implementation.UserEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
class UnusedPasswordResetUserCleanupProcessor implements ItemProcessor<UserEntity, UserEntity> {
    @Override
    public UserEntity process(UserEntity entity) {
        entity.setPasswordResetCode(null);
        return entity;
    }
}
