package codes.dimitri.mediminder.api.user.implementation;

import codes.dimitri.mediminder.api.user.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Mapper
interface UserEntityMapper {
    @Mapping(source = "authorities", target = "authorities", qualifiedByName = "mapAuthorityValues")
    UserDTO toDTO(UserEntity entity, Collection<? extends GrantedAuthority> authorities);

    @Named("mapAuthorityValues")
    static List<String> mapAuthorityValues(Collection<? extends GrantedAuthority> authorities) {
        return authorities
            .stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
    }
}
