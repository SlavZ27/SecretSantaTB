package secretSanta.service.Telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.command.Command;
import secretSanta.entity.User;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;

import java.util.*;

import static secretSanta.Util.Messages.REQUEST_SPLIT_SYMBOL;

@Service
public class SenderButtonServiceTB {

    private final Logger logger = LoggerFactory.getLogger(SenderButtonServiceTB.class);
    private final TelegramBot telegramBot;

    public SenderButtonServiceTB(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void sendCallbackAnswerAndChange(
            Long idChat,
            String command,
            String newText,
            Integer messageId,
            InlineKeyboardMarkup inlineKeyboardMarkup) {
        InlineKeyboardButton[][] inlineKeyboardButtons =
                changeButton(inlineKeyboardMarkup.inlineKeyboard(), command, newText);
        inlineKeyboardMarkup = new InlineKeyboardMarkup(inlineKeyboardButtons);
        EditMessageReplyMarkup editMessageReplyMarkup =
                new EditMessageReplyMarkup(idChat, messageId)
                        .replyMarkup(inlineKeyboardMarkup);
        BaseResponse response = telegramBot.execute(editMessageReplyMarkup);
        if (response != null) {
            if (response.isOk()) {
                logger.debug("ChatId={}; Method sendCallbackAnswer has completed sending the message", idChat);
            } else {
                logger.debug("ChatId={}; Method sendCallbackAnswer received an error : {}",
                        idChat, response.errorCode());
            }
        } else {
            logger.debug("ChatId={}; Method sendCallbackAnswer don't received response",
                    idChat);
        }
    }

    private InlineKeyboardButton[][] changeButton(
            InlineKeyboardButton[][] buttons,
            String dataButton,
            String newText) {
        for (int i = 0; i < buttons.length; i++) {
            for (int i1 = 0; i1 < buttons[i].length; i1++) {
                if (buttons[i][i1].callbackData().equals(dataButton)) {
                    buttons[i][i1] = new InlineKeyboardButton(newText)
                            .callbackData(buttons[i][i1].callbackData());
                    return buttons;
                }
            }
        }
        return buttons;
    }

    public void deleteButton(
            Long idChat,
            Integer messageId) {
        if (messageId == null || idChat == null) {
            return;
        }
        EditMessageReplyMarkup editMessageReplyMarkup =
                new EditMessageReplyMarkup(idChat, messageId)
                        .replyMarkup(new InlineKeyboardMarkup());
        BaseResponse response = telegramBot.execute(editMessageReplyMarkup);
        if (response != null) {
            if (response.isOk()) {
                logger.debug("ChatId={}; Method sendCallbackAnswer has completed sending the message", idChat);
            } else {
                logger.debug("ChatId={}; Method sendCallbackAnswer received an error : {}",
                        idChat, response.errorCode());
            }
        } else {
            logger.debug("ChatId={}; Method sendCallbackAnswer don't received response",
                    idChat);
        }
    }

    public void sendCallbackAnswer(String callbackQueryId) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQueryId);
        BaseResponse response = telegramBot.execute(answerCallbackQuery);
        if (response != null) {
            if (response.isOk()) {
                logger.debug("Method sendCallbackAnswer has completed callbackQueryId = {}", callbackQueryId);
            } else {
                logger.debug("Method sendCallbackAnswer has completed callbackQueryId = {} received an error : {}",
                        callbackQueryId, response.errorCode());
            }
        } else {
            logger.debug("Method sendCallbackAnswer has completed callbackQueryId = {}  don't received response",
                    callbackQueryId);
        }
    }

    public void sendButtonsUnderKeyboard(
            Long idChat,
            String caption,
            List<String> nameButtons,
            List<String> dataButtons,
            int width, int height) {
        logger.info("ChatId={}; Method sendButtonsUnderKeyboard was started for send buttons", idChat);
        if (nameButtons.size() != dataButtons.size()) {
            logger.debug("ChatId={}; Method sendButtonsUnderKeyboard detect different size of Lists", idChat);
            return;
        }
        KeyboardButton[][] keyboardButton = new KeyboardButton[height][width];
        int indexLists = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (indexLists < nameButtons.size()) {
                    keyboardButton[i][j] = new KeyboardButton(nameButtons.get(indexLists));
//                            .callbackData(dataButtons.get(indexLists));              //todo
                } else {
                    keyboardButton[i][j] = new KeyboardButton(Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());
//                            .callbackData(Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());   //todo
                }
                indexLists++;
            }
        }
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardButton);
        SendMessage message = new SendMessage(idChat, caption).replyMarkup(replyKeyboardMarkup);
        SendResponse response = telegramBot.execute(message);
        if (response == null) {
            logger.debug("ChatId={}; Method sendButtonsUnderKeyboard did not receive a response", idChat);
            return;
        } else if (response.isOk()) {
            logger.debug("ChatId={}; Method sendButtonsUnderKeyboard has completed sending the message", idChat);
        } else {
            logger.debug("ChatId={}; Method sendButtonsUnderKeyboard received an error : {}",
                    idChat, response.errorCode());
        }
    }

    public void sendButtonsWithDifferentData(
            Long idChat,
            String caption,
            LinkedHashMap<String, String> buttons,
            int width, int height) {
        if (buttons.isEmpty()) {
            return;
        }
        InlineKeyboardButton[][] tableButtons = new InlineKeyboardButton[height][width];
        Iterator<Map.Entry<String, String>> iterator = buttons.entrySet().iterator();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (iterator.hasNext()) {
                    Map.Entry<String, String> next = iterator.next();
                    tableButtons[i][j] = new InlineKeyboardButton(next.getKey())
                            .callbackData(next.getValue());
                } else {
                    tableButtons[i][j] = new InlineKeyboardButton(Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getNameButton())
                            .callbackData(Command.EMPTY_CALLBACK_DATA_FOR_BUTTON.getTextCommand());
                }
            }
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(tableButtons);
        SendMessage message = new SendMessage(idChat, caption).replyMarkup(inlineKeyboardMarkup);
        SendResponse response = telegramBot.execute(message);
        if (response == null) {
            logger.debug("ChatId={}; Method sendButtonsWithDifferentData did not receive a response", idChat);
        } else if (response.isOk()) {
            logger.debug("ChatId={}; Method sendButtonsWithDifferentData has completed sending the message", idChat);
        } else {
            logger.debug("ChatId={}; Method sendButtonsWithDifferentData received an error : {}",
                    idChat, response.errorCode());
        }
    }

    public void sendButtonsToChatForUser(User user) {
//        Chat chat = user.getChatTelegram();
//        logger.info("ChatId={}; Method sendListCommandForChat was started for send list of command", chat.getId());
//        Pair<List<String>, List<String>> nameAndDataOfButtons = Pair.of(new ArrayList<>(), new ArrayList<>());
////                commandServiceTB.getListsNameButtonAndListsDataButtonForRoleExcludeHide(chat.getIndexMenu());
//
//        List<String> nameList = nameAndDataOfButtons.getFirst();
//        List<String> dataList = nameAndDataOfButtons.getSecond();
//        int countButtons = nameList.size();
//
//        if (countButtons == 0) {
//            logger.debug("ChatId={}; Method sendButtonsCommandForChat detected count of command = 0", chat.getId());
//            return;
//        }
//        Pair<Integer, Integer> widthAndHeight = getTableSize(countButtons);
//        int width = widthAndHeight.getFirst();
//        int height = widthAndHeight.getSecond();
//        sendButtonsWithDifferentData(
//                chat.getId(),
//                MESSAGE_SELECT_COMMAND,
//                nameList,
//                dataList,
//                width, height);
    }

    public void sendMessageWithButtonCancel(Long idChat, String message, String nameButton) {
        sendButtonsWithDifferentData(
                idChat,
                message,
                new LinkedHashMap<>(Map.of(nameButton, Command.CLOSE_UNFINISHED_REQUEST.getTextCommand())),
                1, 1
        );
    }

    public void sendMessageWithOneButtonUnderKeyboard(Long idChat, String message, String nameButton) {
        sendButtonsUnderKeyboard(
                idChat,
                message,
                Collections.singletonList(nameButton),
                Collections.singletonList(Command.CLOSE_UNFINISHED_REQUEST.getTextCommand()),
                1, 1
        );
    }

    public void sendMessageWithOneButton(Long idChat, String message, String nameButton, String dataButton) {
        sendButtonsWithDifferentData(
                idChat,
                message,
                new LinkedHashMap<>(Map.of(nameButton, dataButton)),
                1, 1
        );
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

    public LinkedHashMap<String, String> getButtons(TreeSet<Command> commands) {
        return getButtons(commands, "");
    }

    public LinkedHashMap<String, String> getButtons(TreeSet<Command> commands, String suffixData) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>(commands.size());
        for (Command command : commands) {
            result.put(command.getNameButton(), command.getTextCommand() + REQUEST_SPLIT_SYMBOL + suffixData);
        }
        return result;
    }
}

