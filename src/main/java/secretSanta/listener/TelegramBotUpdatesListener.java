package secretSanta.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import secretSanta.service.Telegram.UpdatesServiceTB;

import jakarta.annotation.PostConstruct;

import java.util.List;

/**
 * The class implementing the UpdateListener interface of the Pengrad library
 * Class engaged in communicating with Telegram services
 * and sending the received list of {@link Update} objects
 * to the {@link UpdatesServiceTB#processUpdate(Update)} class for processing.
 * At the end of the method {@link TelegramBotUpdatesListener#process(List)}, objects are marked as processed, despite possible errors.
 */
@Service
@ConditionalOnProperty(prefix = "telegram.bot",name = "enable",havingValue = "true")
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final TelegramBot telegramBot;
    private final UpdatesServiceTB updatesServiceTB;

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    public TelegramBotUpdatesListener(TelegramBot telegramBot, UpdatesServiceTB updatesServiceTB) {
        this.telegramBot = telegramBot;
        this.updatesServiceTB = updatesServiceTB;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    /**
     * The method sends the received objects as a list
     * to the {@link UpdatesServiceTB#processUpdate(Update)} for processing.
     * At the end of the processing method {@link TelegramBotUpdatesListener#process(List)},
     * objects are marked as processed, despite possible errors.
     *
     * @param updates
     */
    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.debug("Processing update: {}", update);
                updatesServiceTB.processUpdate(update);
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }
    }
}
