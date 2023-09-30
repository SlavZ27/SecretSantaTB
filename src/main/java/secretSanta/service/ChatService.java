package secretSanta.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import secretSanta.entity.Chat;
import secretSanta.entity.User;
import secretSanta.entityDto.ChatDto;
import secretSanta.exception.ChatNotFoundException;
import secretSanta.mapper.ChatMapper;
import secretSanta.repository.ChatRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The class is designed for the operation of the Call Request Service class
 * An important parameter that affects the operation of the CALL_REQUEST CALL_CLIENT command.
 */
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatMapper chatMapper;

    public ChatService(ChatRepository chatRepository,
                       ChatMapper chatMapper) {
        this.chatRepository = chatRepository;
        this.chatMapper = chatMapper;
    }

     public ChatDto createChat(ChatDto chatDto) {
        logger.info("Method createChat was start for create new Chat");
        return chatMapper.chatToChatDto(chatRepository.save(chatMapper.chatDtoToChat(chatDto)));
    }

    public Chat addChat(Chat chat) {
        //Chat.ID always must present
        if (chat.getId() == null) {
            throw new IllegalArgumentException("chat.getId() must be null");
        }
        return chatRepository.save(chat);
    }

    public Chat updateChat(Chat chat) {
        if (chat.getId() == null) {
            throw new IllegalArgumentException("chat.getId() must not be null");
        }
        return chatRepository.save(chat);
    }

    public Chat linkChatUser(Chat chat, User user) {
        if (chat.getId() == null) {
            throw new IllegalArgumentException("chat.getId() must not be null");
        }
        logger.info("Method linkChatUser was start for create link between user and Chat");
        chat.setUser(user);
        return chatRepository.save(chat);
    }

    public ChatDto readChat(Long id) {
        logger.info("Method readChat was start for find Chat by id");
        return chatMapper.chatToChatDto(
                chatRepository.findById(id).
                        orElseThrow(() -> new ChatNotFoundException(String.valueOf(id))));
    }

    public Chat findChat(Long id) {
        logger.info("Method readChat was start for find Chat by id");
        return chatRepository.findById(id).
                orElseThrow(() -> new ChatNotFoundException(String.valueOf(id)));
    }

    public Chat findChatWithUnfinishedRequest(Long id) {
        return chatRepository.getChatByIdWithUnfinishedRequest(id).
                orElseThrow(() -> new ChatNotFoundException(String.valueOf(id)));
    }

    public ChatDto deleteChat(Long id) {
        Chat chat = new Chat();
        chat.setId(id);
        return chatMapper.chatToChatDto(deleteChat(chat));
    }

    public Chat deleteChat(Chat chat) {
        logger.info("Method deleteChat was start for delete Chat");
        if (chat.getId() == null) {
            throw new IllegalArgumentException("Incorrect id chat");
        }
        Chat chatFound = chatRepository.findById(chat.getId()).
                orElseThrow(() -> new ChatNotFoundException(String.valueOf(chat.getId())));
        chatRepository.delete(chatFound);
        return chatFound;
    }

    public List<ChatDto> getAll() {
        logger.info("Method getAll was start for return all Chats");
        return chatRepository.findAll().stream().
                map(chatMapper::chatToChatDto).collect(Collectors.toList());
    }

}
