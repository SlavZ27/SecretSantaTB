package secretSanta.service.Telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;

import static secretSanta.Util.Messages.*;

@Service
public class SenderPhotoServiceTB {

    private final Logger logger = LoggerFactory.getLogger(SenderPhotoServiceTB.class);
    private final TelegramBot telegramBot;
    private final CommandServiceTB commandServiceTB;

    public SenderPhotoServiceTB(TelegramBot telegramBot, CommandServiceTB commandServiceTB) {
        this.telegramBot = telegramBot;
        this.commandServiceTB = commandServiceTB;
    }

    public void sendPhotoFS(Long idChat, String pathFile) throws IOException {
        Path path = Paths.get(pathFile);
        byte[] file = Files.readAllBytes(path);
        SendPhoto sendPhoto = new SendPhoto(idChat, file);
        telegramBot.execute(sendPhoto);
    }

    public void sendPhoto(Long idChat, String idMedia) {
        SendPhoto sendPhoto = new SendPhoto(idChat, idMedia);
        telegramBot.execute(sendPhoto);
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

