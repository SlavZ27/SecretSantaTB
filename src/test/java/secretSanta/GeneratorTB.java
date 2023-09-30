package secretSanta;

import com.github.javafaker.Faker;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import secretSanta.Util.Messages;
import secretSanta.command.Command;
import secretSanta.entity.*;
import secretSanta.service.Telegram.UpdateMapperTB;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.Mockito.times;

public class GeneratorTB {

    public enum UpdateData {MESSAGE, CALLBACK_QUERY}

    private final Faker faker = new Faker();
    private final Random random = new Random();

    public InlineKeyboardMarkup getInlineKeyboardMarkup(InlineKeyboardButton[][] inline_keyboard) {
        return new InlineKeyboardMarkup(inline_keyboard);
    }

    public List<String> getListNameAndData(LinkedHashMap<String, String> buttons) {
        List<String> buttonsNameAndData = new ArrayList<>();
        for (Map.Entry<String, String> entry : buttons.entrySet()) {
            buttonsNameAndData.add("text='" + entry.getKey());
            buttonsNameAndData.add("callback_data='" + entry.getValue());
        }
        return buttonsNameAndData;
    }

    public List<String> getListNameAndData(TreeSet<Command> commands) {
        List<String> buttonsNameAndData = new ArrayList<>();
        for (Command command : commands) {
            buttonsNameAndData.add("text='" + command.getNameButton());
            buttonsNameAndData.add("callback_data='" + command.getTextCommand());
        }
        return buttonsNameAndData;
    }

    public List<String> getListNameAndDataPlusStr(TreeSet<Command> commands, String s) {
        List<String> buttonsNameAndData = new ArrayList<>();
        for (Command command : commands) {
            buttonsNameAndData.add("text='" + command.getNameButton());
            buttonsNameAndData.add("callback_data='" + command.getTextCommand() + Messages.REQUEST_SPLIT_SYMBOL + s);
        }
        return buttonsNameAndData;
    }

    public List<String> getListNameAndData(Command command) {
        List<String> buttonsNameAndData = new ArrayList<>();
        buttonsNameAndData.add("text='" + command.getNameButton());
        buttonsNameAndData.add("callback_data='" + command.getTextCommand());
        return buttonsNameAndData;
    }

    public boolean checkMessageContainData(SendMessage message, List<String> buttonsNameAndDataCheck) {
        String replyMarkup1 = message.getParameters().get("reply_markup").toString();
        for (String s : buttonsNameAndDataCheck) {
            if (!replyMarkup1.contains(s)) {
                System.out.println("replyMarkup1 don't contains: " + s);
                return false;
            }
        }
        return true;
    }

    public boolean checkMessageDontContainData(SendMessage message, List<String> buttonsNameAndDataCheck) {
        String replyMarkup1 = message.getParameters().get("reply_markup").toString();
        for (String s : buttonsNameAndDataCheck) {
            if (replyMarkup1.contains(s)) {
                System.out.println("replyMarkup1 contains: " + s);
                return true;
            }
        }
        return false;
    }

    public boolean checkMessageDontContainButton(SendMessage message) {
        Object o = message.getParameters().get("reply_markup");
        if (o != null) {
            System.out.println("Exist object: " + o);
            return false;
        }
        return true;
    }

    public boolean checkMessageContainButton(SendMessage message) {
        Object o = message.getParameters().get("reply_markup");
        if (o == null) {
            System.out.println("Don't exist reply_markup");
            return false;
        }
        return true;
    }

    public PhotoSize[] getPhotoSize(String[] fileId) {
        PhotoSize[] photoSize = new PhotoSize[fileId.length];
        for (int i = 0; i < fileId.length; i++) {
            setField(photoSize[i], "fileId", fileId[i]);
        }
        return photoSize;
    }

    public User mapToUser(Chat chat) {
        User user = new User(chat.getId());
        setField(user, "first_name", chat.getFirstNameUser());
        setField(user, "last_name", chat.getLastNameUser());
        setField(user, "username", chat.getUserNameTelegram());
        return user;
    }

    public Update getUpdate(User user, String text, UpdateData updateData) {
        if (updateData == null) {
            throw new IllegalArgumentException("updateData must be not null");
        }
        return switch (updateData) {
            case MESSAGE -> getUpdate(getMessage(user, text, null, null), null);
            case CALLBACK_QUERY -> getUpdate(null, getCallbackQuery(user, null, text));
        };
    }

    public Message getMessage(
            com.pengrad.telegrambot.model.User user,
            String text,
            InlineKeyboardMarkup reply_markup,
            PhotoSize photoSize) {
        Message message = new Message();
        setField(message, "from", user);
        setField(message, "text", text);
        setField(message, "reply_markup", reply_markup);
        setField(message, "photo", photoSize);
        return message;
    }

    public CallbackQuery getCallbackQuery(com.pengrad.telegrambot.model.User user,
                                          String inline_message_id,
                                          String data) {
        CallbackQuery callbackQuery = new CallbackQuery();
        setField(callbackQuery, "from", user);
        setField(callbackQuery, "inline_message_id", inline_message_id);
        setField(callbackQuery, "data", data);
        return callbackQuery;
    }

    public Update getUpdate(
            Message message,
            CallbackQuery callbackQuery) {
        Update update = new Update();
        setField(update, "message", message);
        setField(update, "callback_query", callbackQuery);
        return update;
    }

    private Object setField(Object to, String field, Object what) {
        try {
            Field toField = to.getClass().getDeclaredField(field);
            toField.setAccessible(true);
            toField.set(to, what);
            return to;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
