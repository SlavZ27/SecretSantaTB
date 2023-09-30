package secretSanta.lisiner;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import secretSanta.GeneratorEntity;
import secretSanta.GeneratorTB;
import secretSanta.Util.Messages;
import secretSanta.command.Command;
import secretSanta.entity.Cell;
import secretSanta.entity.Chat;
import secretSanta.entity.User;
import secretSanta.entity.UserAlias;
import secretSanta.listener.TelegramBotUpdatesListener;
import secretSanta.repository.*;
import secretSanta.security.EncoderService;
import secretSanta.service.Telegram.CommandServiceTB;
import secretSanta.service.Telegram.SecretSantaServiceTB;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static secretSanta.Util.Messages.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("testGenerateData")
public class TelBotCommandCellTest {
    @Value("${telegram.bot.link}")
    private String telegramBotLink;
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
    private EncoderService encoderService;
    @Autowired
    @InjectMocks
    private TelegramBotUpdatesListener telegramBotUpdatesListener;
    private final GeneratorTB generatorTB = new GeneratorTB();
    private final Random random = new Random();

    @Test
    public void CREATE_GROUPTest() {
        //Проверяемая команда
        String command = Command.CREATE_GROUP.getTextCommand();
        //новый пользователь без всего
        User user = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chat = user.getChatTelegram();
        String nameGroup = generatorEntity.generateUsername();
        String mailingDate = generatorEntity.generateDate(true, LocalDate.now()).toString();
        String aliasName = generatorEntity.generateFirstName();
        String aliasDream = generatorEntity.generateDream();
        //кнопка Cancel
        LinkedHashMap<String, String> buttonCancel = new LinkedHashMap<>(Map.of(NAME_BUTTON_FOR_CANCEL, Command.CLOSE_UNFINISHED_REQUEST.getTextCommand()));
        List<String> listNameAndDataCancel = generatorTB.getListNameAndData(buttonCancel);
        //кнопка к группе
        LinkedHashMap<String, String> buttonDone = new LinkedHashMap<>(Map.of(NAME_BUTTON_TO_GROUP, Command.MANAGE_CELL.getTextCommand() + REQUEST_SPLIT_SYMBOL));
        List<String> listNameAndDataButtonDone = generatorTB.getListNameAndData(buttonDone);
        //первое сообщение. пустое
        Update update1 = generatorTB.getUpdate(generatorTB.mapToUser(chat),
                command, GeneratorTB.UpdateData.MESSAGE);
        String messageCheck1 = MESSAGE_WRITE_GROUP_NAME;
        //второе сообщение. посылаем nameGroup
        Update update2 = generatorTB.getUpdate(generatorTB.mapToUser(chat),
                command + REQUEST_SPLIT_SYMBOL + nameGroup, GeneratorTB.UpdateData.MESSAGE);
        String messageCheck2 = MESSAGE_SELECT_DATE_DISTRIBUTION_PARTICIPANTS;
        //третье сообщение. посылаем mailingDate
        Update update3 = generatorTB.getUpdate(generatorTB.mapToUser(chat),
                command + REQUEST_SPLIT_SYMBOL + mailingDate, GeneratorTB.UpdateData.MESSAGE);
        String messageCheck3_1 = MESSAGE_FINISH_DATE.formatted("");
        String messageCheck3_2 = MESSAGE_WRITE_NAME_SANTA;
        //четвертое сообщение. посылаем aliasName
        Update update4 = generatorTB.getUpdate(generatorTB.mapToUser(chat),
                command + REQUEST_SPLIT_SYMBOL + aliasName, GeneratorTB.UpdateData.MESSAGE);
        String messageCheck4 = MESSAGE_WRITE_DREAMS_SANTA;
        //пятое сообщение. посылаем aliasDream
        Update update5 = generatorTB.getUpdate(generatorTB.mapToUser(chat),
                command + REQUEST_SPLIT_SYMBOL + aliasDream, GeneratorTB.UpdateData.MESSAGE);
        String messageCheck5_1 = MESSAGE_WAS_CREATED.formatted(nameGroup) + "\n"
                                 + MESSAGE_LINK_TO_BOT + telegramBotLink;
        String messageCheck5_2 = MESSAGE_DONE_SAVE_SANTA;

        //запоминаем состояние репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();

        //исполнение
        telegramBotUpdatesListener.process(List.of(update1, update2, update3, update4, update5));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(7)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(7);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck1);
        //с кнопкой Cancel
        assertThat(generatorTB.checkMessageContainData(messages.get(0), listNameAndDataCancel)).isTrue();
        //проверяем сообщения 2
        assertThat(messages.get(1).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(1).getParameters().get("text")).isEqualTo(messageCheck2);
        //с календарем
        assertThat(generatorTB.checkMessageContainButton(messages.get(1))).isTrue();
        //проверяем сообщения 3
        assertThat(messages.get(2).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(2).getParameters().get("text").toString().startsWith(messageCheck3_1)).isTrue();
        assertThat(messages.get(3).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(3).getParameters().get("text")).isEqualTo(messageCheck3_2);
        //без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(2))).isTrue();
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(3))).isTrue();
        //проверяем сообщения 4
        assertThat(messages.get(4).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(4).getParameters().get("text")).isEqualTo(messageCheck4);
        //без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(4))).isTrue();
        //проверяем сообщения 5
        assertThat(messages.get(5).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(5).getParameters().get("text")).isEqualTo(messageCheck5_1);
        assertThat(messages.get(6).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(6).getParameters().get("text")).isNotNull();
        //с кнопкой done
        assertThat(generatorTB.checkMessageContainData(messages.get(5), listNameAndDataButtonDone)).isTrue();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, Map.of(
                GeneratorEntity.NameRepo.USER_ALIAS_REPOSITORY.name(), 1,
                GeneratorEntity.NameRepo.CELL_REPOSITORY.name(), 1));
        //проверяем сохраненные данные
        List<UserAlias> aliases = userAliasRepository.findByUser(user.getId());
        assertThat(aliases.size()).isEqualTo(1);
        UserAlias userAlias = aliases.get(0);
        assertThat(userAlias).isNotNull();
        Cell cell = cellRepository.findByUserAlias(userAlias.getId()).orElse(null);
        assertThat(cell).isNotNull();
        assert cell != null;
        assertThat(cell.getOwner().getId().equals(userAlias.getId())).isTrue();
        List<UserAlias> userAliases = userAliasRepository.findByCell(cell.getId());
        assertThat(userAliases.contains(userAlias)).isTrue();
        assertThat(userAlias.getDreams().equals(aliasDream)).isTrue();
        assertThat(userAlias.getDisplayName().equals(aliasName)).isTrue();
        assertThat(userAlias.getUser().getId().equals(user.getId())).isTrue();
    }

    @Test
    public void JOIN_GROUPTest() {
        //Проверяемая команда
        String command = Command.JOIN_GROUP.getTextCommand();
        //новый пользователь без всего
        User user = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chat = user.getChatTelegram();
        Cell cell = generatorEntity.findRandomCell();
        String aliasName = generatorEntity.generateFirstName();
        String aliasDream = generatorEntity.generateDream();
        //для того чтобы зайти в группу нужен токен
        //сгенерируем его и добавим в группу
        String token = encoderService.generateToken();
        String hash = encoderService.getHash(token);
        cell.setTokenDB(encoderService.getTokenDB(token));
        cell.setTokenHash(hash);
        cell = cellRepository.save(cell);
        //кнопка Cancel
        LinkedHashMap<String, String> buttonCancel = new LinkedHashMap<>(Map.of(NAME_BUTTON_FOR_CANCEL, Command.CLOSE_UNFINISHED_REQUEST.getTextCommand()));
        List<String> listNameAndDataCancel = generatorTB.getListNameAndData(buttonCancel);
        //кнопка к группе
        LinkedHashMap<String, String> buttonDone = new LinkedHashMap<>(Map.of(NAME_BUTTON_TO_GROUP, Command.MANAGE_CELL.getTextCommand() + REQUEST_SPLIT_SYMBOL));
        List<String> listNameAndDataButtonDone = generatorTB.getListNameAndData(buttonDone);
        //первое сообщение. пустое
        Update update1 = generatorTB.getUpdate(generatorTB.mapToUser(chat),
                command, GeneratorTB.UpdateData.MESSAGE);
        String messageCheck1 = MESSAGE_WRITE_CODE_YOUR_GROUP;
        //второе сообщение. посылаем токен
        Update update2 = generatorTB.getUpdate(generatorTB.mapToUser(chat),
                command + REQUEST_SPLIT_SYMBOL + token, GeneratorTB.UpdateData.MESSAGE);
        String messageCheck2 = MESSAGE_JOIN_GROUP_DONE.formatted(cell.getName()) + "\n"
                               + MESSAGE_WRITE_NAME_SANTA;
        //третье сообщение. посылаем aliasName
        Update update3 = generatorTB.getUpdate(generatorTB.mapToUser(chat),
                command + REQUEST_SPLIT_SYMBOL + aliasName, GeneratorTB.UpdateData.MESSAGE);
        String messageCheck3 = MESSAGE_WRITE_DREAMS_SANTA;
        //третье сообщение. посылаем aliasDream
        Update update4 = generatorTB.getUpdate(generatorTB.mapToUser(chat),
                command + REQUEST_SPLIT_SYMBOL + aliasDream, GeneratorTB.UpdateData.MESSAGE);
        String messageCheck4 = MESSAGE_DONE_SAVE_SANTA;

        //запоминаем состояние репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();

        //исполнение
        telegramBotUpdatesListener.process(List.of(update1, update2, update3, update4));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(4)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(4);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck1);
        //с кнопкой Cancel
        assertThat(generatorTB.checkMessageContainData(messages.get(0), listNameAndDataCancel)).isTrue();
        //проверяем сообщения 2
        assertThat(messages.get(1).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(1).getParameters().get("text")).isEqualTo(messageCheck2);
        //без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(1))).isTrue();
        //проверяем сообщения 3
        assertThat(messages.get(2).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(2).getParameters().get("text")).isEqualTo(messageCheck3);
        //с кнопкой Cancel
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(2))).isTrue();
        //проверяем сообщения 4
        assertThat(messages.get(3).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(3).getParameters().get("text")).isEqualTo(messageCheck4);
        //с кнопкой done
        assertThat(generatorTB.checkMessageContainData(messages.get(3), listNameAndDataButtonDone)).isTrue();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, Map.of(
                GeneratorEntity.NameRepo.USER_ALIAS_REPOSITORY.name(), 1));
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
