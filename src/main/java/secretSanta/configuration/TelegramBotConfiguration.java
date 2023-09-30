package secretSanta.configuration;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.DeleteMyCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The class is needed to take the token from the file, create and configure bean {@link TelegramBot}
 */
@Configuration
public class TelegramBotConfiguration {
    /**
     * Token is taken from the file by reference using an annotation @Value
     */
    @Value("${telegram.bot.token}")
    private String token;

    /**
     * Bean is created using an annotation @Bean
     * @return {@link TelegramBot}
     */
    @Bean
    public TelegramBot telegramBot() {
        TelegramBot bot = new TelegramBot(token);
        bot.execute(new DeleteMyCommands());
        return bot;
    }

}
