package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.UserDTO;
import org.mapstruct.Mapper;

@Mapper
interface UserEntityMapper {
    UserDTO toDTO(UserEntity entity);
}
