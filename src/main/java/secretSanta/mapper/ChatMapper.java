package secretSanta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import secretSanta.entity.Chat;
import secretSanta.entityDto.ChatDto;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    @Mapping(target = "id", source = "chat.id")
    @Mapping(target = "userNameTelegram", source = "chat.userNameTelegram")
    @Mapping(target = "firstNameUser", source = "chat.firstNameUser")
    @Mapping(target = "lastNameUser", source = "chat.lastNameUser")
    @Mapping(target = "lastActivity", source = "chat.lastActivity")
    ChatDto chatToChatDto(Chat chat);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userNameTelegram", source = "userNameTelegram")
    @Mapping(target = "firstNameUser", source = "firstNameUser")
    @Mapping(target = "lastNameUser", source = "lastNameUser")
    @Mapping(target = "lastActivity", source = "lastActivity")
    Chat chatDtoToChat(ChatDto chat);

}
