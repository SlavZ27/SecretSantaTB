package secretSanta.service.Telegram;

import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.command.Command;
import secretSanta.entity.Chat;
import secretSanta.entity.OngoingRequestTB;
import secretSanta.entity.User;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;

import java.util.Optional;
import java.util.TreeSet;

import static secretSanta.Util.Messages.MESSAGE_SORRY_I_DONT_KNOW_COMMAND;
import static secretSanta.Util.Messages.MESSAGE_SORRY_I_KNOW_THIS;

@Service
public class TechServiceTB {
    private final Logger logger = LoggerFactory.getLogger(TechServiceTB.class);
    private final RequestServiceTB requestServiceTB;
    private final NavigateServiceTB navigateServiceTB;
    private final SenderMessageServiceTB senderMessageServiceTB;
    private final SenderButtonServiceTB senderButtonServiceTB;
    private final SenderCommandMenuServiceTB senderCommandMenuServiceTB;
    private final CommandServiceTB commandServiceTB;

    public TechServiceTB(RequestServiceTB requestServiceTB, NavigateServiceTB navigateServiceTB, SenderMessageServiceTB senderMessageServiceTB, SenderButtonServiceTB senderButtonServiceTB, SenderCommandMenuServiceTB senderCommandMenuServiceTB, CommandServiceTB commandServiceTB) {
        this.requestServiceTB = requestServiceTB;
        this.navigateServiceTB = navigateServiceTB;
        this.senderMessageServiceTB = senderMessageServiceTB;
        this.senderButtonServiceTB = senderButtonServiceTB;
        this.senderCommandMenuServiceTB = senderCommandMenuServiceTB;
        this.commandServiceTB = commandServiceTB;
    }

    @PostConstruct
    public void setCommandsMenu() {
        TreeSet<Command> functionCommand = commandServiceTB.getFunctionCommand();
        senderCommandMenuServiceTB.setCommandsMenu(functionCommand);
    }

    public void sendCallbackAnswer(String callbackQueryId) {
        senderButtonServiceTB.sendCallbackAnswer(callbackQueryId);
    }

    public void closeUnfinishedRequest(Update update) {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        OngoingRequestTB requestTB = requestServiceTB.del(chat);
        if (requestTB == null) {
            return;
        }
        Integer messageId = UpdateMapperTB.getMessageId(update);
        if (messageId != null) {
            senderButtonServiceTB.deleteButton(chat.getId(), messageId);
        }
    }

    public OngoingRequestTB getUnfinishedRequestForChat() {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        return requestServiceTB.find(chat);
    }

    public void sendSorryIKnowThis() {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_SORRY_I_KNOW_THIS);
        senderButtonServiceTB.sendButtonsToChatForUser(user);
    }

    public void sendUnknownProcess() {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        senderMessageServiceTB.sendMessage(chat.getId(),
                MESSAGE_SORRY_I_DONT_KNOW_COMMAND);
        senderButtonServiceTB.sendButtonsToChatForUser(user);
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
