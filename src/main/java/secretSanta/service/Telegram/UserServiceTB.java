package secretSanta.service.Telegram;

import com.pengrad.telegrambot.model.Update;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.command.Command;
import secretSanta.entity.*;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;
import secretSanta.service.SecretSantaService;
import secretSanta.service.UserAliasService;
import secretSanta.service.UserService;

import java.util.HashMap;
import java.util.Optional;

import static secretSanta.Util.Messages.*;

@Service
public class UserServiceTB {
    private final RequestServiceTB requestServiceTB;
    private final SenderMessageServiceTB senderMessageServiceTB;
    private final SenderButtonServiceTB senderButtonServiceTB;
    private final UserService userService;
    private final UserAliasService userAliasService;
    private final SecretSantaService secretSantaService;

    public UserServiceTB(RequestServiceTB requestServiceTB, SenderMessageServiceTB senderMessageServiceTB, SenderButtonServiceTB senderButtonServiceTB, UserService userService, UserAliasService userAliasService, SecretSantaService secretSantaService) {
        this.requestServiceTB = requestServiceTB;
        this.senderMessageServiceTB = senderMessageServiceTB;
        this.senderButtonServiceTB = senderButtonServiceTB;
        this.userService = userService;
        this.userAliasService = userAliasService;
        this.secretSantaService = secretSantaService;
    }

    public void changeDream(Update update) {
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        if (message == null || message.isBlank()) {
            return;
        }
        String paramAliasId = "aliasDream";
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        OngoingRequestTB ongoingRequestTB = chat.getOngoingRequestTB();
        String userAliasIdParam = null;
        if (ongoingRequestTB != null) {
            HashMap<String, String> params = ongoingRequestTB.getParams();
            userAliasIdParam = params.get(paramAliasId);
        }
        if (userAliasIdParam == null) {
            String[] split = message.split(REQUEST_SPLIT_SYMBOL);
            Long userAliasId = null;
            StringBuilder dream = new StringBuilder();
            if (split.length > 0) {
                try {
                    userAliasId = Long.parseLong(split[0]);
                } catch (NumberFormatException e) {
                    senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_FAIL_ARGUMENT);
                    return;
                }
            }
            if (split.length > 1) {
                for (int i = 1; i < split.length; i++) {
                    dream.append(split[i]);
                    if (i + 1 < split.length) {
                        dream.append(" ");
                    }
                }
            }
            if (dream.toString().isBlank()) {
                requestServiceTB.save(chat, Command.CHANGE_ALIAS_DREAM,
                        Pair.of(paramAliasId, String.valueOf(userAliasId)));
                senderMessageServiceTB.sendMessage(chat.getId(),
                        MESSAGE_WRITE_DREAMS_SANTA);
            } else {
                UserAlias userAlias = userAliasService.findById(userAliasId).orElse(null);
                if (userAlias != null) {
                    userAlias.setDreams(dream.toString());
                    userAlias = userAliasService.update(userAlias);
                    if (userAlias != null) {
                        requestServiceTB.del(chat);
                        senderMessageServiceTB.sendMessage(chat.getId(),
                                MESSAGE_YOUR_DREAM_OK + userAlias.getDreams());
                    }
                }
            }
            return;
        }
        Long userAliasId = null;
        try {
            userAliasId = Long.parseLong(userAliasIdParam);
        } catch (NumberFormatException e) {
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_FAIL_ARGUMENT);
            return;
        }
        UserAlias userAlias = userAliasService.findById(userAliasId).orElse(null);
        boolean sendSanta = false;
        if (userAlias != null) {
            userAlias.setDreams(message);
            if (userAlias.isRequestDreamRecipient()) {
                userAlias.setRequestDreamRecipient(false);
                sendSanta = true;
            }
            userAlias = userAliasService.update(userAlias);
            if (userAlias != null) {
                requestServiceTB.del(chat);
                senderMessageServiceTB.sendMessage(chat.getId(),
                        MESSAGE_YOUR_DREAM_OK.formatted(userAlias.getDreams()));
            }
        }
        if (sendSanta) {
            UserAlias santa = secretSantaService.getSantaOfRecipient(userAlias);
            Long id = santa.getUser().getChatTelegram().getId();
            senderButtonServiceTB.sendMessageWithOneButton(
                    id,
                    MESSAGE_WARD_HAS_BEEN_UPDATED,
                    NAME_BUTTON_WARD,
                    Command.WARD.getTextCommand() + REQUEST_SPLIT_SYMBOL + santa.getId());
        }
    }

    public void changeAliasName(Update update) {
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        if (message == null || message.isBlank()) {
            return;
        }
        String paramAliasId = "aliasId";
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        OngoingRequestTB ongoingRequestTB = chat.getOngoingRequestTB();
        String userAliasIdParam = null;
        if (ongoingRequestTB != null) {
            HashMap<String, String> params = ongoingRequestTB.getParams();
            userAliasIdParam = params.get(paramAliasId);
        }
        if (userAliasIdParam == null) {
            String[] split = message.split(REQUEST_SPLIT_SYMBOL);
            Long userAliasId = null;
            StringBuilder name = new StringBuilder();
            if (split.length > 0) {
                try {
                    userAliasId = Long.parseLong(split[0]);
                } catch (NumberFormatException e) {
                    senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_FAIL_ARGUMENT);
                    return;
                }
            }
            if (split.length > 1) {
                for (int i = 1; i < split.length; i++) {
                    name.append(split[i]);
                    if (i + 1 < split.length) {
                        name.append(" ");
                    }
                }
            }
            if (name.toString().isBlank()) {
                requestServiceTB.save(chat, Command.CHANGE_ALIAS_NAME,
                        Pair.of(paramAliasId, String.valueOf(userAliasId)));
                senderMessageServiceTB.sendMessage(chat.getId(),
                        MESSAGE_WRITE_NAME_SANTA);
            } else {
                UserAlias userAlias = userAliasService.findById(userAliasId).orElse(null);
                if (userAlias != null) {
                    userAlias.setDisplayName(name.toString());
                    userAlias = userAliasService.update(userAlias);
                    if (userAlias != null) {
                        requestServiceTB.del(chat);
                        senderMessageServiceTB.sendMessage(chat.getId(),
                                MESSAGE_YOUR_NAME_OK.formatted(userAlias.getDisplayName()));
                    }
                }
            }
            return;
        }
        Long userAliasId = null;
        try {
            userAliasId = Long.parseLong(userAliasIdParam);
        } catch (NumberFormatException e) {
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_FAIL_ARGUMENT);
            return;
        }
        UserAlias userAlias = userAliasService.findById(userAliasId).orElse(null);
        if (userAlias != null) {
            userAlias.setDisplayName(message);
            userAlias = userAliasService.update(userAlias);
            if (userAlias != null) {
                requestServiceTB.del(chat);
                senderMessageServiceTB.sendMessage(chat.getId(),
                        MESSAGE_YOUR_NAME_OK.formatted(userAlias.getDisplayName()));
            }
        }
    }

    private User getCurrentUser() {
        return Optional.ofNullable(
                ((UserDetailsCustom)
                        SecurityContextHolder.getContext().getAuthentication()
                                .getPrincipal())
                        .getUser()
        ).orElseThrow(() -> new UserNotFoundException("User is absent"));
    }
}
