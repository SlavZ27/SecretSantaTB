package secretSanta.lisiner;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import secretSanta.GeneratorEntity;
import secretSanta.GeneratorTB;
import secretSanta.command.Command;
import secretSanta.entity.Cell;
import secretSanta.entity.Chat;
import secretSanta.entity.OngoingRequestTB;
import secretSanta.entity.UserAlias;
import secretSanta.listener.TelegramBotUpdatesListener;
import secretSanta.repository.*;
import secretSanta.service.Telegram.CommandServiceTB;
import secretSanta.service.Telegram.SecretSantaServiceTB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static secretSanta.Util.Messages.MESSAGE_SORRY_I_DONT_KNOW_COMMAND;
import static secretSanta.Util.Messages.REQUEST_SPLIT_SYMBOL;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("testGenerateData")
public class TelBotCommandTechTest {

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
    public void CLOSE_UNFINISHED_REQUESTTest() {
        String command = Command.CLOSE_UNFINISHED_REQUEST.getTextCommand();
        Chat chat = generatorEntity.findRandomChat();
        //Создаём запись в бд для удаления
        OngoingRequestTB ongoingRequestTB = new OngoingRequestTB();
        ongoingRequestTB.setChat(chat);
        ongoingRequestTB.setCommand(Command.CREATE_GROUP);
        ongoingRequestTB.setId(chat.getId());
        OngoingRequestTB requestTB = ongoingRequestTBRepository.save(ongoingRequestTB);
        //запоминаем состояние репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        //исполнение
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command, GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        Mockito.verify(telegramBot, times(0)).execute(argumentCaptor.capture());
        //состояние репозиториев
        assertThat(generatorEntity.checkRepositoryState(repositoryState, Map.of(
                GeneratorEntity.NameRepo.ONGOINGREQUEST_TB_REPOSITORY.name(), -1))).isTrue();
        assertThat(ongoingRequestTBRepository.findById(requestTB.getId()).isEmpty()).isTrue();
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
