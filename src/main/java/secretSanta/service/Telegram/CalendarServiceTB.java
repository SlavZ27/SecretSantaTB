package secretSanta.service.Telegram;

import com.pengrad.telegrambot.model.Update;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.command.Command;
import secretSanta.entity.Chat;
import secretSanta.entity.User;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static secretSanta.Util.Messages.MESSAGE_SELECT_DATE_DISTRIBUTION_PARTICIPANTS;
import static secretSanta.Util.Messages.REQUEST_SPLIT_SYMBOL;

@Service
public class CalendarServiceTB {
    private final SenderCalendarServiceTB senderCalendarServiceTB;
    private static final String m1 = "января";
    private static final String m2 = "февраля";
    private static final String m3 = "марта";
    private static final String m4 = "апреля";
    private static final String m6 = "мая";
    private static final String m7 = "июня";
    private static final String m5 = "июля";
    private static final String m8 = "августа";
    private static final String m9 = "сентября";
    private static final String m10 = "октября";
    private static final String m11 = "ноября";
    private static final String m12 = "декабря";
    private static final String mn1 = "Январь";
    private static final String mn2 = "Февраль";
    private static final String mn3 = "Март";
    private static final String mn4 = "Апрель";
    private static final String mn6 = "Май";
    private static final String mn7 = "Июнь";
    private static final String mn5 = "Июль";
    private static final String mn8 = "Август";
    private static final String mn9 = "Сентябрь";
    private static final String mn10 = "Октябрь";
    private static final String mn11 = "Ноябрь";
    private static final String mn12 = "Декабрь";

    public CalendarServiceTB(SenderCalendarServiceTB senderCalendarServiceTB) {
        this.senderCalendarServiceTB = senderCalendarServiceTB;
    }

    public static String toRus(LocalDate localDate) {
        return localDate.getDayOfMonth() + " " + getMonth2(localDate.getMonthValue()) + " " + localDate.getYear() + "г.";
    }
    public static String toRus(int month) {
        return getMonth1(month);
    }

    private static String getMonth1(int month) {
        return switch (month) {
            case 1 -> mn1;
            case 2 -> mn2;
            case 3 -> mn3;
            case 4 -> mn4;
            case 5 -> mn5;
            case 6 -> mn6;
            case 7 -> mn7;
            case 8 -> mn8;
            case 9 -> mn9;
            case 10 -> mn10;
            case 11 -> mn11;
            case 12 -> mn12;
            default -> "";
        };
    }
    private static String getMonth2(int month) {
        return switch (month) {
            case 1 -> m1;
            case 2 -> m2;
            case 3 -> m3;
            case 4 -> m4;
            case 5 -> m5;
            case 6 -> m6;
            case 7 -> m7;
            case 8 -> m8;
            case 9 -> m9;
            case 10 -> m10;
            case 11 -> m11;
            case 12 -> m12;
            default -> "";
        };
    }

    public void changeCalendar(Update update) {
        User user = getCurrentUser();
        Chat chat = user.getChatTelegram();
        String message = UpdateMapperTB.getMessageWithoutCommand(update);
        if (message == null || message.isBlank()) {
            return;
        }
        String[] split = message.split(REQUEST_SPLIT_SYMBOL);
        if (split.length < 2) {
            return;
        }
        Command command = Command.fromStringUpperCase(split[0]);
        LocalDate localDate = null;
        try {
            localDate = LocalDate.parse(split[1]);
        } catch (DateTimeParseException ignored) {
        }
        if (command == null || localDate == null) {
            return;
        }
        senderCalendarServiceTB.sendMonthCalendar(
                chat.getId(),
                MESSAGE_SELECT_DATE_DISTRIBUTION_PARTICIPANTS,
                localDate,
                Command.CREATE_GROUP,
                UpdateMapperTB.getMessageId(update));
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
