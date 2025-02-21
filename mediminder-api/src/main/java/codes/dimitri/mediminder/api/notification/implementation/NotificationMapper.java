package codes.dimitri.mediminder.api.notification.implementation;

import codes.dimitri.mediminder.api.notification.NotificationDTO;
import org.mapstruct.Mapper;

@Mapper
public interface NotificationMapper {
    NotificationDTO toDTO(NotificationEntity entity);
}
