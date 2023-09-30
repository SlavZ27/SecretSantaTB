package secretSanta.service.Telegram;

import com.pengrad.telegrambot.model.Update;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.command.Command;
import secretSanta.entity.*;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;
import secretSanta.service.CellService;
import secretSanta.service.ChatService;
import secretSanta.service.UserAliasService;

import java.util.*;

import static secretSanta.Util.Messages.*;

@Service
public class NavigateServiceTB {
    private final SenderButtonServiceTB senderButtonServiceTB;
    private final ChatService chatService;
    private final UserAliasService userAliasService;
    private final CellService cellService;
    private final SenderCommandMenuServiceTB senderCommandMenuServiceTB;
    private final SenderMessageServiceTB senderMessageServiceTB;
    private final CommandServiceTB commandServiceTB;

    public NavigateServiceTB(SenderButtonServiceTB senderButtonServiceTB, ChatService chatService, UserAliasService userAliasService, CellService cellService, SenderCommandMenuServiceTB senderCommandMenuServiceTB, SenderMessageServiceTB senderMessageServiceTB, CommandServiceTB commandServiceTB) {
        this.senderButtonServiceTB = senderButtonServiceTB;
        this.chatService = chatService;
        this.userAliasService = userAliasService;
        this.cellService = cellService;
        this.senderCommandMenuServiceTB = senderCommandMenuServiceTB;
        this.senderMessageServiceTB = senderMessageServiceTB;
        this.commandServiceTB = commandServiceTB;
    }

    public void menuStart0() {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        TreeSet<Command> commands = commandServiceTB.getCommands(0);
        if (userAliasService.countUserAliases(user) == 0) {
            commands.remove(Command.MY_CELLS);
        }
        LinkedHashMap<String, String> buttons = senderButtonServiceTB.getButtons(commands);
        Pair<Integer, Integer> widthAndHeight = getTableSize(buttons.size());
        senderMessageServiceTB.sendHello();
        senderButtonServiceTB.sendButtonsWithDifferentData(
                chat.getId(),
                MESSAGE_SELECT_COMMAND,
                buttons,
                widthAndHeight.getFirst(), widthAndHeight.getSecond());
    }

    public void menuMyCells3() {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        List<UserAlias> userAliases = userAliasService.findByUser(user);
        int sizeAliases = userAliases.size();
        if (sizeAliases == 0) {
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_YOU_DONT_HAVE_GROUP);
            return;
        }
        if (sizeAliases == 1) {
            menuManageCell3(userAliases.get(0));
            return;
        }
        LinkedHashMap<String, String> buttons = new LinkedHashMap<>();
        userAliases.forEach(userAlias -> buttons.put(
                userAlias.getCell().getName(),
                Command.MANAGE_CELL.getTextCommand() + REQUEST_SPLIT_SYMBOL + userAlias.getId()));
        senderButtonServiceTB.sendButtonsWithDifferentData(
                chat.getId(),
                MESSAGE_SELECT_GROUP,
                buttons,
                1, buttons.size());
    }

    public void menuManageCell3(Update update) {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        long userAliasId;
        try {
            userAliasId = Long.parseLong(message);
        } catch (NumberFormatException e) {
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_FAIL_ARGUMENT);
            return;
        }
        Optional<UserAlias> userAlias = userAliasService.findById(userAliasId);
        if (userAlias.isEmpty() ||
            userAlias.get().getUser() == null ||
            userAlias.get().getUser().getId() == null ||
            !userAlias.get().getUser().getId().equals(user.getId())) {
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_YOU_DONT_HAVE_THIS_GROUP);
            return;
        }
        menuManageCell3(userAlias.get());
    }

    public void menuManageCell3(UserAlias userAlias) {
        User user = getCurrentUser();
        if (!user.getId().equals(userAlias.getUser().getId())) {
            return;
        }
        Chat chat = user.getChatTelegram();
        Cell cell = userAlias.getCell();
        TreeSet<Command> commands = commandServiceTB.getCommands(3);
        if (!cell.getLock()) {
            commands.remove(Command.WARD);
        } else {
            commands.remove(Command.CALC_CELL);
        }
        LinkedHashMap<String, String> buttons = senderButtonServiceTB.getButtons(commands, userAlias.getId() + "");
        senderButtonServiceTB.sendButtonsWithDifferentData(
                chat.getId(),
                cell.getName() + "\n" + MESSAGE_SELECT_COMMAND,
                buttons,
                1, buttons.size());
    }

    public void menuMyData4(Update update) {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        long userAliasId;
        try {
            userAliasId = Long.parseLong(message);
        } catch (NumberFormatException e) {
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_FAIL_ARGUMENT);
            return;
        }
        UserAlias userAlias = userAliasService.findById(userAliasId).orElse(null);
        if (userAlias == null ||
            userAlias.getUser() == null ||
            userAlias.getUser().getId() == null ||
            !userAlias.getUser().getId().equals(user.getId())) {
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_YOU_DONT_HAVE_THIS_GROUP);
            return;
        }
        TreeSet<Command> commands = commandServiceTB.getCommands(4);
        LinkedHashMap<String, String> buttons = senderButtonServiceTB.getButtons(commands, userAlias.getId() + "");
        senderButtonServiceTB.sendButtonsWithDifferentData(
                chat.getId(),
                userAlias.getDisplayName() + "\n" + MESSAGE_SELECT_COMMAND,
                buttons,
                1, buttons.size());
    }

    public void menuWard5(Update update) {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        long userAliasId;
        try {
            userAliasId = Long.parseLong(message);
        } catch (NumberFormatException e) {
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_FAIL_ARGUMENT);
            return;
        }
        UserAlias userAlias = userAliasService.findById(userAliasId).orElse(null);
        if (userAlias == null ||
            userAlias.getUser() == null ||
            userAlias.getUser().getId() == null ||
            !userAlias.getUser().getId().equals(user.getId())) {
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_YOU_DONT_HAVE_THIS_GROUP);
            return;
        }
        UserAlias recipient = userAlias.getRecipient();
        if (recipient != null) {
            LinkedHashMap<String, String> buttons = senderButtonServiceTB.getButtons(
                    commandServiceTB.getCommands(5),
                    String.valueOf(userAliasId));
            String messageText =
                    MESSAGE_WARD_SANTA.formatted(recipient.getDisplayName()) +
                    recipient.getDreams();
            senderButtonServiceTB.sendButtonsWithDifferentData(
                    chat.getId(),
                    messageText,
                    buttons,
                    2, 1);
        } else {
            Cell cell = userAlias.getCell();
            senderButtonServiceTB.sendMessageWithOneButton(
                    chat.getId(),
                    MESSAGE_WARD_NOT_YET.formatted(cell.getMailingDate().toString()),
                    BUTTON_BACK,
                    Command.MANAGE_CELL.getTextCommand() + REQUEST_SPLIT_SYMBOL + userAlias.getId()
            );
        }
    }

    public Pair<Integer, Integer> getTableSize(int countElements) {
//        int width = 0;
//        int height = 0;
//        if (countElements == 1) {
//            width = 1;
//            height = 1;
//        } else if (countElements > 4) {
//            width = 4;
//            if (countElements % 4 == 0) {
//                height = countElements / 4;
//            } else {
//                height = countElements / 4 + 1;
//            }
//        } else if (countElements % 4 == 0) {
//            width = 4;
//            height = countElements / 4;
//        } else if (countElements % 3 == 0) {
//            width = 3;
//            height = countElements / 3;
//        } else if (countElements % 2 == 0) {
//            width = 2;
//            height = countElements / 2;
//        }
        return Pair.of(1, countElements);
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
