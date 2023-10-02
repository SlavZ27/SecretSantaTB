package secretSanta.service.Telegram;

import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.command.Command;
import secretSanta.entity.*;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.EncoderService;
import secretSanta.security.UserDetailsCustom;
import secretSanta.service.CellService;
import secretSanta.service.UserAliasService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static secretSanta.Util.Messages.*;

@Service
public class CellServiceTB {
    private final String telegramBotLink;
    private final RequestServiceTB requestServiceTB;
    private final SenderCalendarServiceTB senderCalendarServiceTB;
    private final SenderMessageServiceTB senderMessageServiceTB;
    private final SenderButtonServiceTB senderButtonServiceTB;
    private final EncoderService encoderService;
    private final CellService cellService;
    private final UserAliasService userAliasService;

    public CellServiceTB(
            @Value("${telegram.bot.link}") String telegramBotLink,
            RequestServiceTB requestServiceTB,
            SenderCalendarServiceTB senderCalendarServiceTB,
            SenderMessageServiceTB senderMessageServiceTB,
            SenderButtonServiceTB senderButtonServiceTB,
            EncoderService encoderService,
            CellService cellService,
            UserAliasService userAliasService) {
        this.telegramBotLink = telegramBotLink;
        this.requestServiceTB = requestServiceTB;
        this.senderCalendarServiceTB = senderCalendarServiceTB;
        this.senderMessageServiceTB = senderMessageServiceTB;
        this.senderButtonServiceTB = senderButtonServiceTB;
        this.encoderService = encoderService;
        this.cellService = cellService;
        this.userAliasService = userAliasService;
    }

    public void createGroup(Update update) {
        String paramNameGroup = "name";
        String paramMailingDate = "mailingDate";
        String paramEndDate = "endDate";
        String paramAliasName = "aliasName";
//        String paramAliasDream = "aliasDream";
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        //start procedure
        if (message.isBlank()) {
            requestServiceTB.save(chat, Command.CREATE_GROUP);
            senderButtonServiceTB.sendMessageWithButtonCancel(
                    chat.getId(),
                    MESSAGE_WRITE_GROUP_NAME,
                    NAME_BUTTON_FOR_CANCEL);
            return;
        }

        OngoingRequestTB ongoingRequestTB = chat.getOngoingRequestTB();
        HashMap<String, String> params = ongoingRequestTB.getParams();
        String nameGroup = params.get(paramNameGroup);
        String mailingDate = params.get(paramMailingDate);
        String endDate = params.get(paramEndDate);
        String aliasName = params.get(paramAliasName);
        //nameGroup
        if (nameGroup == null) {
            requestServiceTB.save(chat, Command.CREATE_GROUP, Pair.of(paramNameGroup, message));
            //show calendar to select date
            senderCalendarServiceTB.sendMonthCalendar(
                    chat.getId(),
                    MESSAGE_SELECT_DATE_DISTRIBUTION_PARTICIPANTS,
                    LocalDate.now(),
                    Command.CREATE_GROUP,
                    null);
            return;
        }
        //mailingDate
        if (mailingDate == null) {
            LocalDate md;
            try {
                md = LocalDate.parse(message);              //mailingDate
            } catch (DateTimeParseException e) {
                return;
            }
            LocalDate ed = md.plusMonths(3);    //endDate
            LocalDate nextNY = LocalDate.of(md.getYear(), 1, 1);
            if (ed.isBefore(nextNY)) {
                ed = nextNY.plusMonths(2);
            }
            requestServiceTB.save(chat, Command.CREATE_GROUP,
                    Pair.of(paramNameGroup, nameGroup),
                    Pair.of(paramMailingDate, md.toString()),
                    Pair.of(paramEndDate, ed.toString()));
            senderButtonServiceTB.deleteButton(chat.getId(), UpdateMapperTB.getMessageId(update));
            senderMessageServiceTB.sendMessage(chat.getId(),
                    MESSAGE_FINISH_DATE.formatted(CalendarServiceTB.toRus(md)));
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_WRITE_NAME_SANTA);
            return;
        }
        //aliasName
        if (aliasName == null) {
            requestServiceTB.save(chat, Command.CREATE_GROUP,
                    Pair.of(paramNameGroup, nameGroup),
                    Pair.of(paramMailingDate, mailingDate),
                    Pair.of(paramEndDate, endDate),
                    Pair.of(paramAliasName, message));
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_WRITE_DREAMS_SANTA);
            return;
        }
        //aliasDream
        if (!message.isBlank()) {
            String token = encoderService.generateToken();
            String hash = encoderService.getHash(token);
            UserAlias userAlias = new UserAlias();
            userAlias.setDisplayName(aliasName);
            userAlias.setDreams(message);
            userAlias.setUser(user);
//            userAlias.setCell(cell);
            userAlias = userAliasService.addUserAlias(userAlias);
            Cell cell = new Cell();
            cell.setName(nameGroup);
            cell.setOwner(userAlias);
            cell.setCreateDate(LocalDate.now());
            cell.setMailingDate(LocalDate.parse(mailingDate));
            cell.setEndDate(LocalDate.parse(endDate));
            cell.setLock(false);
            cell.setTokenDB(encoderService.getTokenDB(token));
            cell.setTokenHash(hash);
            cell = cellService.addCell(cell);
            userAlias.setCell(cell);
            userAlias = userAliasService.update(userAlias);
            requestServiceTB.del(chat);
            senderButtonServiceTB.sendMessageWithOneButton(chat.getId(),
                    MESSAGE_WAS_CREATED.formatted(cell.getName()) + "\n"
                            + MESSAGE_LINK_TO_BOT.formatted(telegramBotLink),
                    NAME_BUTTON_TO_GROUP,
                    Command.MANAGE_CELL.getTextCommand() + REQUEST_SPLIT_SYMBOL + userAlias.getId());
            senderMessageServiceTB.sendMessage(chat.getId(), token);
        }
    }


    public void joinGroup(Update update) {
        String paramIdCell = "idCell";
        String paramIdAlias = "idAlias";
        String paramAliasName = "aliasName";
//        String paramAliasDream = "aliasDream";
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        //start procedure
        if (message.isBlank()) {
            requestServiceTB.save(chat, Command.JOIN_GROUP);
            senderButtonServiceTB.sendMessageWithButtonCancel(
                    chat.getId(),
                    MESSAGE_WRITE_CODE_YOUR_GROUP,
                    NAME_BUTTON_FOR_CANCEL);
            return;
        }
        OngoingRequestTB ongoingRequestTB = chat.getOngoingRequestTB();
        HashMap<String, String> params = ongoingRequestTB.getParams();
        String idCell = params.get(paramIdCell);
        String idAlias = params.get(paramIdAlias);
        String aliasName = params.get(paramAliasName);
        //token
        if (idCell == null || idAlias == null) {
//          token = message
            String tokenDB = encoderService.getTokenDB(message);
            Cell cell = tokenDB != null ? cellService.findByTokenDB(tokenDB) : null;
            if (cell == null || !encoderService.matches(message, cell.getTokenHash())) {
                senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_TOKEN_FAIL);
                return;
            }
            if (cellService.exist(user, cell)) {
                UserAlias userAlias = userAliasService.findByUserAndCellIncludeDeleted(user, cell).orElse(null);
                if (userAlias != null && userAlias.getEnable() != null) {
                    if (userAlias.getEnable()) {
                        senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_YOU_ALREADY_JOIN);
                    } else {
                        userAliasService.enable(userAlias);
                        senderMessageServiceTB.sendMessage(chat.getId(),
                                MESSAGE_JOIN_GROUP_DONE.formatted(cell.getName()));
                    }
                }
                return;
            }
            if (cell.getLock()) {
                senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_LOCK_EXIST);
                return;
            }
            UserAlias userAlias = new UserAlias();
            userAlias.setUser(user);
            userAlias.setCell(cell);
            userAlias.setDisplayName(UpdateMapperTB.getDisplayName(update));
            userAlias = userAliasService.addUserAlias(userAlias);
            requestServiceTB.save(chat, Command.JOIN_GROUP,
                    Pair.of(paramIdCell, cell.getId().toString()),
                    Pair.of(paramIdAlias, userAlias.getId().toString()));
            senderMessageServiceTB.sendMessage(chat.getId(),
                    MESSAGE_JOIN_GROUP_DONE.formatted(cell.getName()) + "\n"
                            + MESSAGE_WRITE_NAME_SANTA);
            return;
        }
        //aliasName
        if (aliasName == null) {
            UserAlias userAlias = userAliasService.findById(Long.parseLong(idAlias)).orElse(null);
            if (userAlias != null) {
                userAlias.setDisplayName(message);
                userAlias = userAliasService.update(userAlias);
                requestServiceTB.save(chat, Command.JOIN_GROUP,
                        Pair.of(paramIdCell, idCell),
                        Pair.of(paramIdAlias, idAlias),
                        Pair.of(paramAliasName, userAlias.getDisplayName()));
                senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_WRITE_DREAMS_SANTA);
            }
            return;
        }
        //aliasDream
        if (!message.isBlank()) {
            UserAlias userAlias = userAliasService.findById(Long.parseLong(idAlias)).orElse(null);
            if (userAlias != null) {
                userAlias.setDreams(message);
                userAliasService.update(userAlias);
                requestServiceTB.del(chat);
                senderButtonServiceTB.sendMessageWithOneButton(chat.getId(),
                        MESSAGE_DONE_SAVE_SANTA,
                        NAME_BUTTON_TO_GROUP,
                        Command.MANAGE_CELL.getTextCommand() + REQUEST_SPLIT_SYMBOL + userAlias.getId());
            }
        }
    }

    public void changeAlias(Update update) {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        String message = UpdateMapperTB.getMessageWithoutCommand(update);

        //start procedure
        if (message == null || message.isBlank()) {
            requestServiceTB.save(chat, Command.JOIN_GROUP);
            senderButtonServiceTB.sendMessageWithButtonCancel(
                    chat.getId(),
                    MESSAGE_WRITE_CODE_YOUR_GROUP,
                    NAME_BUTTON_FOR_CANCEL);
            return;
        } else {
            Cell cell = cellService.findByTokenDB(encoderService.getTokenDB(message));
            Optional<UserAlias> userAliasesOpt = userAliasService.findByUserAndCell(user, cell);
            if (userAliasesOpt.isPresent()) {
                senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_YOU_ALREADY_JOIN);
                return;
            }

            if (cell == null || !encoderService.matches(message, cell.getTokenHash())) {
                senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_TOKEN_FAIL);
                return;
            }
            if (cell.getLock()) {
                senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_LOCK_EXIST);
                return;
            }
            UserAlias userAlias = new UserAlias();
            userAlias.setUser(user);
            userAlias.setCell(cell);
            userAlias = userAliasService.addUserAlias(userAlias);

            senderButtonServiceTB.sendMessageWithOneButton(
                    chat.getId(),
                    MESSAGE_JOIN_GROUP_DONE.replace("{}", userAlias.getCell().getName()),
                    NAME_BUTTON_TO_GROUP,
                    Command.CHANGE_ALIAS.getTextCommand());
        }
    }

    public void aboutGroup(Update update) {
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
        Cell cell = cellService.findByUserAlias(userAliasId);
        if (!cellService.exist(user, cell)) {
            return;
        }
        if (cell != null) {
            int countUserAliases = cellService.countUserAliases(cell);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Это группа %s.\n", cell.getName()));
            sb.append(String.format("Её создал пользователь %s.\n", cell.getOwner().getDisplayName()));
            sb.append(String.format("Она была создана %s.\n", cell.getCreateDate()));
            sb.append(String.format("Дата рассылки %s.\n", cell.getMailingDate()));
            sb.append(String.format("Дата прекращения работы группы %s.\n", cell.getEndDate()));
            sb.append(String.format("В этой группе %s участников.\n", countUserAliases));
            senderMessageServiceTB.sendMessage(chat.getId(), sb.toString());
        }
    }

    public void exitGroup(Update update) {
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        if (message == null || message.isBlank()) {
            return;
        }
        String MESSAGE_EXIT_GROUP = "Выйти из группы?";
        String MESSAGE_EXIT_GROUP_NO = "Вы не вышли из группы";
        String MESSAGE_EXIT_GROUP_YES = "Вы вышли из группы";
        String ANSWER_YES = "YES";
        String BUTTON_YES = "Да";
        String ANSWER_NO = "NO";
        String BUTTON_NO = "НЕТ";
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();

        String[] split = message.split(REQUEST_SPLIT_SYMBOL);
        Long userAliasId = null;
        String answer;
        if (split.length > 0) {
            try {
                userAliasId = Long.parseLong(split[0]);
            } catch (NumberFormatException e) {
                senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_FAIL_ARGUMENT);
                return;
            }
        }
        if (split.length == 1) {
            Command command = Command.EXIT_GROUP;
            LinkedHashMap<String, String> buttons = new LinkedHashMap<>();
            buttons.put(BUTTON_YES,
                    command.getTextCommand() + REQUEST_SPLIT_SYMBOL +
                            userAliasId + REQUEST_SPLIT_SYMBOL +
                            ANSWER_YES);
            buttons.put(BUTTON_NO,
                    command.getTextCommand() + REQUEST_SPLIT_SYMBOL +
                            userAliasId + REQUEST_SPLIT_SYMBOL +
                            ANSWER_NO);
            senderButtonServiceTB.sendButtonsWithDifferentData(
                    chat.getId(),
                    MESSAGE_EXIT_GROUP,
                    buttons,
                    2, 1
            );
            return;
        }
        answer = split[1];
        if (ANSWER_NO.equals(answer)) {
            senderButtonServiceTB.deleteButton(chat.getId(), UpdateMapperTB.getMessageId(update));
            senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_EXIT_GROUP_NO);
        }
        if (ANSWER_YES.equals(answer)) {
            senderButtonServiceTB.deleteButton(chat.getId(), UpdateMapperTB.getMessageId(update));
            UserAlias userAlias = userAliasService.findById(userAliasId).orElse(null);
            if (userAlias != null &&
                    userAlias.getUser() != null && userAlias.getUser().getId() != null &&
                    userAlias.getUser().getId().equals(user.getId())) {
                userAliasService.disable(userAlias);
                senderMessageServiceTB.sendMessage(chat.getId(), MESSAGE_EXIT_GROUP_YES);
            }
        }
    }

    public void listParticipants(Update update) {
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
        Cell cell = cellService.findByUserAlias(userAliasId);
        if (cell != null) {
            if (!cellService.exist(user, cell)) {
                return;
            }
            List<UserAlias> userAliases = cellService.getUserAliases(cell);
            if (userAliases.isEmpty()) {
                senderMessageServiceTB.sendMessage(chat.getId(), "В группе нет участников");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("В этой группе %s участников:", userAliases.size()));
            userAliases.forEach(userAlias -> sb.append("\n").append(userAlias.getDisplayName()));
            senderMessageServiceTB.sendMessage(chat.getId(), sb.toString());
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
