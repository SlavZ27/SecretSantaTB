package secretSanta.service;

import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import secretSanta.service.Telegram.CommandServiceTB;
import secretSanta.service.Telegram.SenderButtonServiceTB;


class SenderButtonServiceTBTest {

    @InjectMocks
    @Autowired
    private SenderButtonServiceTB senderButtonServiceTB = new SenderButtonServiceTB(null);
    @Mock
    private TelegramBot telegramBot;
    @Mock
    private CommandServiceTB commandServiceTB;

//    @Test
    void sendMonthCalendarTest() {
//        telegramBotSenderService.sendMonthCalendar(1L, "strMessage", LocalDate.now(), Command.CREATE_GROUP);


    }
}