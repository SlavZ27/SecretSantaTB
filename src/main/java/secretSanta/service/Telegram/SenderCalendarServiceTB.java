package secretSanta.service.Telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.Util.Messages;
import secretSanta.command.Command;
import secretSanta.entity.User;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;

import java.time.LocalDate;
import java.util.Optional;

import static secretSanta.Util.Messages.*;

@Service
public class SenderCalendarServiceTB {

    private final Logger logger = LoggerFactory.getLogger(SenderCalendarServiceTB.class);
    private final TelegramBot telegramBot;
    private final CommandServiceTB commandServiceTB;

    public SenderCalendarServiceTB(TelegramBot telegramBot, CommandServiceTB commandServiceTB) {
        this.telegramBot = telegramBot;
        this.commandServiceTB = commandServiceTB;
    }

    public void sendMonthCalendar(Long idChat, String message, LocalDate dateParam, Command command, Integer messageId) {
        LocalDate date = dateParam != null ? dateParam : LocalDate.now();

        //year - month              1 column
        //days of weak              7 column
        //4-5 rows of days          7 column
        //4-5 rows of days          7 column
        //4-5 rows of days          7 column
        //4-5 rows of days          7 column
        //4-5 rows of days          7 column
        //control                   2 column

        //rows of days
        //detect row count of days
        int countRowOfDays = 0;
        int daysOfMonth = date.lengthOfMonth();
        int firstDayOfMonth = date.withDayOfMonth(1).getDayOfWeek().getValue();

        int countButtons = daysOfMonth + firstDayOfMonth;
        if (daysOfMonth <= 28) {
            countRowOfDays = 4;
        } else if (daysOfMonth <= 35) {
            countRowOfDays = 5;
        } else if (daysOfMonth < 42) {
            countRowOfDays = 6;
        }
        countButtons = countRowOfDays * 7;

        InlineKeyboardButton[][] rowsDays = new InlineKeyboardButton[countRowOfDays][7];
        int rowIndex = 0;
        int columnIndex = 0;
        int dayIndex = 2 - firstDayOfMonth;
        int countButtonIndex = 0;
        while (countButtonIndex < countButtons) {
            if ((dayIndex < 1) || (dayIndex > daysOfMonth)) {
                rowsDays[rowIndex][columnIndex] = getInlineKeyboardButton(
                        Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getNameButton(),
                        Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());
            } else {
                LocalDate localDate = LocalDate.of(date.getYear(), date.getMonth(), dayIndex);
                rowsDays[rowIndex][columnIndex] = getInlineKeyboardButton(
                        String.valueOf(dayIndex),
                        command.getTextCommand() + REQUEST_SPLIT_SYMBOL + localDate);
            }
            dayIndex++;
            columnIndex++;
            countButtonIndex++;
            if (columnIndex >= 7) {
                columnIndex = 0;
                rowIndex++;
            }
        }

        //row control
        InlineKeyboardButton[] rowControl = new InlineKeyboardButton[3];
        rowControl[0] = getInlineKeyboardButton(
                Messages.PREV,
                Command.CHANGE_CALENDAR.getTextCommand()
                        + REQUEST_SPLIT_SYMBOL
                        + command
                        + REQUEST_SPLIT_SYMBOL
                        + date.minusMonths(1));
        rowControl[1] = getInlineKeyboardButton(
                Messages.NOW,
                Command.CHANGE_CALENDAR.getTextCommand()
                        + REQUEST_SPLIT_SYMBOL
                        + command
                        + REQUEST_SPLIT_SYMBOL
                        + LocalDate.now());
        rowControl[2] = getInlineKeyboardButton(
                NEXT,
                Command.CHANGE_CALENDAR.getTextCommand()
                        + REQUEST_SPLIT_SYMBOL
                        + command
                        + REQUEST_SPLIT_SYMBOL
                        + date.plusMonths(1));

        //row days of weak
        InlineKeyboardButton[] rowDaysOfWeak = new InlineKeyboardButton[7];
        rowDaysOfWeak[0] = getInlineKeyboardButton(C_MON, Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());
        rowDaysOfWeak[1] = getInlineKeyboardButton(C_TUES, Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());
        rowDaysOfWeak[2] = getInlineKeyboardButton(C_WED, Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());
        rowDaysOfWeak[3] = getInlineKeyboardButton(C_THUR, Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());
        rowDaysOfWeak[4] = getInlineKeyboardButton(C_FRI, Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());
        rowDaysOfWeak[5] = getInlineKeyboardButton(C_SA, Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());
        rowDaysOfWeak[6] = getInlineKeyboardButton(C_SU, Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());

        //row year - month
        InlineKeyboardButton[] rowYearMonth = new InlineKeyboardButton[1];
        rowYearMonth[0] = getInlineKeyboardButton(
                CalendarServiceTB.toRus(date.getMonthValue()) + " " + date.getYear(),
                Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());

        //result    rowControl + rowsDays + rowYearMonth + rowDaysOfWeak
        int countRow = 1 + rowsDays.length + 1 + 1;
        InlineKeyboardButton[][] tableButtons = new InlineKeyboardButton[countRow][];
        int index = 0;
        tableButtons[index++] = rowYearMonth;
        tableButtons[index++] = rowDaysOfWeak;
        for (InlineKeyboardButton[] rowsDay : rowsDays) {
            tableButtons[index++] = rowsDay;
        }
        tableButtons[index] = rowControl;

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(tableButtons);

        BaseResponse response;
        if (messageId != null) {
            EditMessageReplyMarkup editMessageReplyMarkup =
                    new EditMessageReplyMarkup(idChat, messageId)
                            .replyMarkup(inlineKeyboardMarkup);
            response = telegramBot.execute(editMessageReplyMarkup);
        } else {
            SendMessage sendMessage = new SendMessage(idChat, message).replyMarkup(inlineKeyboardMarkup);
            response = telegramBot.execute(sendMessage);
        }

        if (response != null) {
            if (response.isOk()) {
                logger.debug("ChatId={}; Method sendButtonsWithCommonData has completed sending the message", idChat);
            } else {
                logger.debug("ChatId={}; Method sendButtonsWithCommonData received an error : {}",
                        idChat, response.errorCode());
            }
        } else {
            logger.debug("ChatId={}; Method sendButtonsWithCommonData don't received response",
                    idChat);
        }
    }

    private InlineKeyboardButton getInlineKeyboardButton(String textButton, String callbackData) {
        return new InlineKeyboardButton(textButton)
                .callbackData(callbackData);
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

