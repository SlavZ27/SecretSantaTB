package secretSanta.service.Telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import secretSanta.command.Command;

import java.util.List;
import java.util.TreeSet;

@Service
public class SenderCommandMenuServiceTB {

    private final Logger logger = LoggerFactory.getLogger(SenderCommandMenuServiceTB.class);
    private final TelegramBot telegramBot;
    private final CommandServiceTB commandServiceTB;

    public SenderCommandMenuServiceTB(TelegramBot telegramBot, CommandServiceTB commandServiceTB) {
        this.telegramBot = telegramBot;
        this.commandServiceTB = commandServiceTB;
    }

    public void setCommandsMenu(TreeSet<Command> functionCommands) {
        BotCommand[] botCommands = new BotCommand[functionCommands.size()];
        int i = 0;
        for (Command command : functionCommands) {
            String element;
            String data = command.getTextCommand();
            if (data.startsWith("/")) {
                element = data.substring(1);
            } else {
                element = data;
            }
            botCommands[i++] = new BotCommand(element.toLowerCase(), command.getNameButton());
        }
        SetMyCommands setMyCommands = new SetMyCommands(botCommands);
        BaseResponse response = telegramBot.execute(setMyCommands);
        if (response != null) {
            if (response.isOk()) {
                logger.debug("Method setCommandsMenu has completed sending the message");
            } else {
                logger.debug("Method setCommandsMenu received an error : {}", response.errorCode());
            }
        } else {
            logger.debug("Method setCommandsMenu don't received response");
        }
    }

}

