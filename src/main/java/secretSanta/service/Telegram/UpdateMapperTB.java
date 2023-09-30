package secretSanta.service.Telegram;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import secretSanta.command.Command;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;

import java.util.Optional;

import static secretSanta.Util.Messages.REQUEST_SPLIT_SYMBOL;

public final class UpdateMapperTB {

    private UpdateMapperTB() {
    }

    public static Long getIdChat(Update update) {
        Long id = Optional.ofNullable(update.message())
                .map(m -> m.from().id())
                .orElse(null);
        if (id == null) {
            id = Optional.ofNullable(update.callbackQuery())
                    .map(c -> c.from().id())
                    .orElse(null);
        }
        if (id == null || id < 0) {
            return null;
        }
        return id;
    }

    public static String getFirstName(Update update) {
        String fName = Optional.ofNullable(update.message())
                .map(m -> m.from().firstName())
                .orElse(null);
        if (fName == null) {
            fName = Optional.ofNullable(update.callbackQuery())
                    .map(c -> c.from().firstName())
                    .orElse(null);
        }
        if (fName == null || fName.isBlank()) {
            return null;
        }
        return fName;
    }

    public static String getLastName(Update update) {
        String lName = Optional.ofNullable(update.message())
                .map(m -> m.from().lastName())
                .orElse(null);
        if (lName == null) {
            lName = Optional.ofNullable(update.callbackQuery())
                    .map(c -> c.from().lastName())
                    .orElse(null);
        }
        if (lName == null || lName.isBlank()) {
            return null;
        }
        return lName;
    }

    public static String getUsername(Update update) {
        String uName = Optional.ofNullable(update.message())
                .map(m -> m.from().username())
                .orElse(null);
        if (uName == null) {
            uName = Optional.ofNullable(update.callbackQuery())
                    .map(c -> c.from().username())
                    .orElse(null);
        }
        if (uName == null || uName.isBlank()) {
            return null;
        }
        return uName;
    }

    public static String getMessage(Update update) {
        String message = Optional.ofNullable(update.message())
                .map(m -> m.text())
                .orElse(null);
        if (message == null) {
            message = Optional.ofNullable(update.callbackQuery())
                    .map(c -> c.data())
                    .orElse(null);
        }
        if (message == null) {
            message = Optional.ofNullable(update.message())
                    .map(c -> c.caption())
                    .orElse(null);
        }
        if (message == null || message.isBlank()) {
            return null;
        }
        return message;
    }

    public static String getMessageWithoutCommand(Update update) {
        String message = getMessage(update).trim();
        if (message.startsWith("/")) {
            int index = message.indexOf(REQUEST_SPLIT_SYMBOL);
            if (index > 0) {
                return message.substring(index + 1);
            } else {
                return "";
            }
        }
        return message;
    }

    public static String getIdMedia(Update update) {
        if (update.message().photo() != null) {
            int maxPhotoIndex = update.message().photo().length - 1;
            if (update.message().photo()[maxPhotoIndex].fileId() != null) {
                return update.message().photo()[maxPhotoIndex].fileId();
            }
        }
        return null;
    }

    public static InlineKeyboardMarkup getInlineKeyboardMarkup(Update update) {
        return Optional.ofNullable(update.callbackQuery())
                .map(c -> c.message().replyMarkup())
                .orElse(null);
    }

    public static Integer getMessageId(Update update) {
        return Optional.ofNullable(update.callbackQuery())
                .map(c -> c.message().messageId())
                .orElse(null);
    }

    public static String getCallbackQueryId(Update update) {
        return Optional.ofNullable(update.callbackQuery())
                .map(c -> c.id())
                .orElse(null);
    }

    public static Command getCommand(Update update) {
        String message = getMessage(update);
        if (!message.startsWith("/")) {
            return null;
        }
        int indexSpace = message.indexOf(" ");
        if (indexSpace > 0) {
            message = message.substring(0, indexSpace);
        }
        return Command.fromStringUpperCase(message);
    }


    /**
     * The method checks the string so that it is not null, or empty
     *
     * @param s
     * @return true or false
     */
    public static Boolean isNotNullOrEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    /**
     * The method makes a single word from a string with many words
     *
     * @param s,
     * @param indexWord
     * @return word with indexWord <br>
     * if (s==null) then return null <br>
     * if (indexWord > sum of words into string) then return "" <br>
     * if (string don't contain {@link secretSanta.Util.Messages#REQUEST_SPLIT_SYMBOL}) then return string without changes <br>
     */
    public static String toWord(String s, int indexWord) {
        if (s == null) {
            return null;
        }
        if (!s.contains(REQUEST_SPLIT_SYMBOL)) {
            return s;
        }
        String[] sMas = s.split(REQUEST_SPLIT_SYMBOL);

        if (indexWord >= sMas.length) {
            return "";
        }
        return sMas[indexWord];
    }

    /**
     * This method juxtapose string to long
     *
     * @param message is not null
     * @return long message
     */
    public static Long mapStringToLong(String message) {
        return Long.parseLong(message, 10);
    }

    /**
     * This method juxtapose string to long
     *
     * @param message is not null
     * @return Long message
     */
    public static Integer mapStringToInt(String message) {
        return Integer.parseInt(message, 10);
    }

    public static String getDisplayName(Update update) {
        String firstName = getFirstName(update);
        String lastName = getLastName(update);
        String username = getUsername(update);
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            sb.append(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(lastName);
        }
        if (sb.isEmpty() && !username.isBlank()) {
            sb.append(username);
        }
        if (sb.isEmpty()) {
            String unnamedName = "Unnamed";
            sb.append(unnamedName);
        }
        return sb.toString();
    }

    @Cacheable
    public static Update getUpdate() {
        return Optional.ofNullable(
                ((UserDetailsCustom)
                        SecurityContextHolder.getContext().getAuthentication()
                                .getPrincipal())
                        .getUpdate()
        ).orElseThrow(() -> new UserNotFoundException("User is absent"));
    }

}