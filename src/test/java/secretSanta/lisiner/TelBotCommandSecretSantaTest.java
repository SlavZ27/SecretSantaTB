package secretSanta.lisiner;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import secretSanta.GeneratorEntity;
import secretSanta.GeneratorTB;
import secretSanta.command.Command;
import secretSanta.entity.*;
import secretSanta.listener.TelegramBotUpdatesListener;
import secretSanta.repository.*;
import secretSanta.service.Telegram.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static secretSanta.Util.Messages.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("testGenerateData")
public class TelBotCommandSecretSantaTest {

    @Autowired
    private GeneratorEntity generatorEntity;
    @MockBean
    private TelegramBot telegramBot;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private CellRepository cellRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private OngoingRequestTBRepository ongoingRequestTBRepository;
    @Autowired
    private UserAliasRepository userAliasRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommandServiceTB commandServiceTB;
    @Autowired
    private SecretSantaServiceTB secretSantaServiceTB;
    @Autowired
    @InjectMocks
    private TelegramBotUpdatesListener telegramBotUpdatesListener;
    private final GeneratorTB generatorTB = new GeneratorTB();
    private final Random random = new Random();


    @Test
    public void mailingTest() {
        //Оставляем лишь одну, подходящую по условиям группу
        Cell cell = generatorEntity.setOnlyOneCellMailing();
        //Алиасы группы
        List<UserAlias> userAliases = cellRepository.getUserAliases(cell.getId());
        int sizeCell = userAliases.size();
        assertThat(sizeCell > 0).isTrue();
        //запоминаем состояние репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();

        //исполнение
        secretSantaServiceTB.mailing();

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        //отправка сообщений по количеству участников группы
        Mockito.verify(telegramBot, times(sizeCell)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(sizeCell);

        //проверяем сообщения
        //ИД чатов
        List<String> chatIdAll = new ArrayList<>();
        //Все тексты сообщений
        StringBuilder textAll = new StringBuilder();
        //Все данные кнопок
        StringBuilder replyMarkupAll = new StringBuilder();
        for (SendMessage message : messages) {
            chatIdAll.add(message.getParameters().get("chat_id").toString());
            textAll.append(message.getParameters().get("text").toString());
            replyMarkupAll.append(message.getParameters().get("reply_markup").toString());
        }
        //проверка ИД чатов
        userAliases.stream().map(ua -> ua.getUser().getChatTelegram().getId().toString())
                .forEach(s -> assertThat(chatIdAll.contains(s)).isTrue());
        //проверка сообщений
        String textAllStr = textAll.toString();
        userAliases.stream().map(UserAlias::getDisplayName)
                .forEach(s -> assertThat(textAllStr.contains(s)).isTrue());
        userAliases.stream().map(UserAlias::getDreams)
                .forEach(s -> assertThat(textAllStr.contains(s)).isTrue());
        //проверка кнопок
        List<UserAlias> secretSantaList = userAliasRepository.findByCell(cell.getId());
        String replyMarkupAllStr = replyMarkupAll.toString();
        secretSantaList.stream().map(UserAlias::getId).map(Object::toString)
                .forEach(s -> assertThat(replyMarkupAllStr.contains(
                        "callback_data='%s'".formatted(
                                Command.REQUEST_WISHES.getTextCommand() + REQUEST_SPLIT_SYMBOL + s
                        ))).isTrue());
        //проверка состояние репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
    }

    @Test
    public void unknownCommandTest() {
        String unknownCommand = "/dasfsgdsdh";
        Chat chat = generatorEntity.findRandomChat();

        //запоминаем состояние репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();

        //исполнение
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chat), unknownCommand, GeneratorTB.UpdateData.MESSAGE)));

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(1)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(1);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(MESSAGE_SORRY_I_DONT_KNOW_COMMAND);
        //Без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(0))).isTrue();
        //состояние репозиториев не поменялось
        assertThat(generatorEntity.checkRepositoryState(repositoryState, null)).isTrue();
    }

    @Test
    public void contextLoads() {
        assertThat(telegramBot).isNotNull();
        assertThat(authorityRepository).isNotNull();
        assertThat(cellRepository).isNotNull();
        assertThat(chatRepository).isNotNull();
        assertThat(ongoingRequestTBRepository).isNotNull();
        assertThat(secretSantaServiceTB).isNotNull();
        assertThat(userAliasRepository).isNotNull();
        assertThat(userRepository).isNotNull();
        assertThat(commandServiceTB).isNotNull();
        assertThat(telegramBotUpdatesListener).isNotNull();
    }

    @BeforeEach
    public void generateData() {
        generatorEntity.clearData();
        generatorEntity.generateData(6);
    }

    @AfterEach
    public void clearData() {
        generatorEntity.clearData();
    }
}
