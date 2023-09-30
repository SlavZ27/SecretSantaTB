package secretSanta.service.Telegram;

import com.pengrad.telegrambot.model.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.command.Command;
import secretSanta.entity.*;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;
import secretSanta.service.CellService;
import secretSanta.service.SecretSantaService;
import secretSanta.service.UserAliasService;

import java.util.List;
import java.util.Optional;

import static secretSanta.Util.Messages.*;

@Service
public class SecretSantaServiceTB {
    private final SecretSantaService secretSantaService;
    private final SenderButtonServiceTB senderButtonServiceTB;
    private final SenderMessageServiceTB senderMessageServiceTB;
    private final CellService cellService;
    private final UserAliasService userAliasService;

    public SecretSantaServiceTB(SecretSantaService secretSantaService, SenderButtonServiceTB senderButtonServiceTB, SenderMessageServiceTB senderMessageServiceTB, CellService cellService, UserAliasService userAliasService) {
        this.secretSantaService = secretSantaService;
        this.senderButtonServiceTB = senderButtonServiceTB;
        this.senderMessageServiceTB = senderMessageServiceTB;
        this.cellService = cellService;
        this.userAliasService = userAliasService;
    }

    public void requestWishes(Update update) {
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        if (message == null || message.isBlank()) {
            return;
        }
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        Long idSanta = Long.parseLong(message);
        UserAlias santa = userAliasService.findById(idSanta).orElse(null);
        if (santa == null) {
            return;
        }
        if (!santa.getUser().getId().equals(user.getId())) {
            return;
        }
        UserAlias recipient = santa.getRecipient();
        recipient.setRequestDreamRecipient(true);
        recipient = userAliasService.update(recipient);
        senderButtonServiceTB.sendMessageWithOneButton(
                recipient.getUser().getChatTelegram().getId(),
                MESSAGE_REQUEST_DREAMS,
                NAME_BUTTON_CHANGE_DREAMS,
                Command.CHANGE_ALIAS_DREAM.getTextCommand() + REQUEST_SPLIT_SYMBOL + recipient.getId()
        );
        senderMessageServiceTB.sendMessage(chat.getId(),
                MESSAGE_REQUEST_DREAMS_SEND);
    }

    //          sec min hour day/mon mon day/week
    @Scheduled(cron = "0 0 4 * * *")
    public void mailing() {
        List<Cell> unlockCellForMailing = cellService.findUnlockCellForMailing();
        for (Cell cell : unlockCellForMailing) {
            List<UserAlias> santaList = secretSantaService.calcSecretSanta(cell);
            sending(santaList);
        }
    }

    public void calcCell(Update update) {
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        if (message == null || message.isBlank()) {
            return;
        }
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        Long idUserAlias = Long.parseLong(message);
        Cell cell = cellService.findByUserAlias(idUserAlias);
        if (cell == null) {
            return;
        }
        List<UserAlias> santaList = secretSantaService.calcSecretSanta(cell);
        sending(santaList);
    }

    public void sending(List<UserAlias> santaList) {
        for (UserAlias santa : santaList) {
            UserAlias recipient = santa.getRecipient();
            senderButtonServiceTB.sendMessageWithOneButton(
                    santa.getUser().getChatTelegram().getId(),
                    MESSAGE_MAILING.formatted(
                            recipient.getDisplayName(), recipient.getDreams()),
                    NAME_BUTTON_REQUEST_ADDITIONAL_DREAMS,
                    Command.REQUEST_WISHES.getTextCommand() + REQUEST_SPLIT_SYMBOL + santa.getId());
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
