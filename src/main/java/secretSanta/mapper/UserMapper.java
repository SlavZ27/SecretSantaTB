package secretSanta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import secretSanta.entity.User;
import secretSanta.entityDto.RegisterReqDto;
import secretSanta.entityDto.UserDto;


/**
 * Provides methods for mapping User to Dto`s
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto userToDto(User user);

    User userDtoToUser(UserDto userDto);

    /**
     * RegisterReqDto to User.
     *
     * @param registerReq the register req
     * @param pass        the pass
     * @return {@link User}
     */
    @Mapping(target = "username", source = "registerReq.username")
    @Mapping(target = "password", source = "pass")
    User registerReqToUser(RegisterReqDto registerReq, String pass);

}
