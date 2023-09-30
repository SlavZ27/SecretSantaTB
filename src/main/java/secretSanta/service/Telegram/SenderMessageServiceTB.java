package secretSanta.service.Telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.Util.Messages;
import secretSanta.command.Command;
import secretSanta.entity.Chat;
import secretSanta.entity.User;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static secretSanta.Util.Messages.*;

@Service
public class SenderMessageServiceTB {

    private final Logger logger = LoggerFactory.getLogger(SenderMessageServiceTB.class);
    private final TelegramBot telegramBot;
    private final CommandServiceTB commandServiceTB;

    public SenderMessageServiceTB(TelegramBot telegramBot, CommandServiceTB commandServiceTB) {
        this.telegramBot = telegramBot;
        this.commandServiceTB = commandServiceTB;
    }

    public void sendMessage(Long idChat, String textMessage) {
        logger.debug("ChatId={}; Method sendMessage was started for send a message : {}", idChat, textMessage);
        SendMessage sendMessage = new SendMessage(idChat, textMessage);
        SendResponse response = telegramBot.execute(sendMessage);
        if (response != null) {
            if (response.isOk()) {
                logger.debug("ChatId={}; Method sendMessage has completed sending the message", idChat);
            } else {
                logger.debug("ChatId={}; Method sendMessage received an error : {}",
                        idChat, response.errorCode());
            }
        } else {
            logger.debug("ChatId={}; Method sendMessage don't received response",
                    idChat);
        }
    }

    public void sendHello() {
        User currentUser = getCurrentUser();
        Chat chat = currentUser.getChatTelegram();
        String nameCurrentUser = getNameCurrentUser();
        sendMessage(chat.getId(), MESSAGE_HELLO.formatted(nameCurrentUser));
    }

    private User getCurrentUser() {
        return Optional.ofNullable(
                ((UserDetailsCustom)
                        SecurityContextHolder.getContext().getAuthentication()
                                .getPrincipal())
                        .getUser()
        ).orElseThrow(() -> new UserNotFoundException("User is absent"));
    }

    private String getNameCurrentUser() {
        User user = getCurrentUser();
        return user.getDisplayName();
    }
}

