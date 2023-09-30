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
import secretSanta.Util.Messages;
import secretSanta.command.Command;
import secretSanta.entity.*;
import secretSanta.listener.TelegramBotUpdatesListener;
import secretSanta.repository.*;
import secretSanta.service.Telegram.CommandServiceTB;
import secretSanta.service.Telegram.SecretSantaServiceTB;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static secretSanta.Util.Messages.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("testGenerateData")
public class TelBotCommandNavigateTest {

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
    public void STARTTest() {
        //Проверяемая команда
        String command = Command.START.getTextCommand();
        //пользователь с группами
        User userY = generatorEntity.findRandomUser();
        //новый пользователь без всего
        User userN = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chatY = userY.getChatTelegram();
        Chat chatN = userN.getChatTelegram();
        String messageCheck1 = MESSAGE_HELLO + userY.getDisplayName() + ".\n";
        String messageCheck2And4 = MESSAGE_SELECT_COMMAND;
        String messageCheck3 = MESSAGE_HELLO + userN.getDisplayName() + ".\n";
        //все команды меню
        TreeSet<Command> commands = commandServiceTB.getCommands(0);
        //все динамическая команда
        Command commandMyCells = Command.MY_CELLS;
        commands.remove(commandMyCells);
        //список текста кнопок и сообщения кнопок
        List<String> buttonsNameAndDataCheck = generatorTB.getListNameAndData(commands);
        //список текста кнопок и сообщения динамической кнопки
        List<String> myCellsNameAndData = generatorTB.getListNameAndData(commandMyCells);

        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chatY), command, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chatN), command, GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(4)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(4);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chatY.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck1);
        assertThat(messages.get(1).getParameters().get("chat_id")).isEqualTo(chatY.getId());
        assertThat(messages.get(1).getParameters().get("text")).isEqualTo(messageCheck2And4);
        //проверяем кнопки 1
        assertThat(generatorTB.checkMessageContainData(messages.get(1), buttonsNameAndDataCheck)).isTrue();
        assertThat(generatorTB.checkMessageDontContainData(messages.get(1), myCellsNameAndData)).isTrue();

        //проверяем сообщения 2
        assertThat(messages.get(2).getParameters().get("chat_id")).isEqualTo(chatN.getId());
        assertThat(messages.get(2).getParameters().get("text")).isEqualTo(messageCheck3);
        assertThat(messages.get(3).getParameters().get("chat_id")).isEqualTo(chatN.getId());
        assertThat(messages.get(3).getParameters().get("text")).isEqualTo(messageCheck2And4);
        //проверяем кнопки 2
        assertThat(generatorTB.checkMessageContainData(messages.get(3), buttonsNameAndDataCheck)).isTrue();
        assertThat(generatorTB.checkMessageDontContainData(messages.get(3), myCellsNameAndData)).isFalse();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
    }

    @Test
    public void MENU_BACK3Test() {
        //Проверяемая команда
        String command = Command.MENU_BACK3.getTextCommand();
        //пользователь с группами
        User userY = generatorEntity.findRandomUser();
        //новый пользователь без всего
        User userN = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chatY = userY.getChatTelegram();
        Chat chatN = userN.getChatTelegram();
        String messageCheck1 = MESSAGE_HELLO + userY.getDisplayName() + ".\n";
        String messageCheck2And4 = MESSAGE_SELECT_COMMAND;
        String messageCheck3 = MESSAGE_HELLO + userN.getDisplayName() + ".\n";
        //все команды меню
        TreeSet<Command> commands = commandServiceTB.getCommands(0);
        //все динамическая команда
        Command commandMyCells = Command.MY_CELLS;
        commands.remove(commandMyCells);
        //список текста кнопок и сообщения кнопок
        List<String> buttonsNameAndDataCheck = generatorTB.getListNameAndData(commands);
        //список текста кнопок и сообщения динамической кнопки
        List<String> myCellsNameAndData = generatorTB.getListNameAndData(commandMyCells);

        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chatY), command, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chatN), command, GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(4)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(4);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chatY.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck1);
        assertThat(messages.get(1).getParameters().get("chat_id")).isEqualTo(chatY.getId());
        assertThat(messages.get(1).getParameters().get("text")).isEqualTo(messageCheck2And4);
        //проверяем кнопки 1
        assertThat(generatorTB.checkMessageContainData(messages.get(1), buttonsNameAndDataCheck)).isTrue();
        assertThat(generatorTB.checkMessageDontContainData(messages.get(1), myCellsNameAndData)).isTrue();

        //проверяем сообщения 2
        assertThat(messages.get(2).getParameters().get("chat_id")).isEqualTo(chatN.getId());
        assertThat(messages.get(2).getParameters().get("text")).isEqualTo(messageCheck3);
        assertThat(messages.get(3).getParameters().get("chat_id")).isEqualTo(chatN.getId());
        assertThat(messages.get(3).getParameters().get("text")).isEqualTo(messageCheck2And4);
        //проверяем кнопки 2
        assertThat(generatorTB.checkMessageContainData(messages.get(3), buttonsNameAndDataCheck)).isTrue();
        assertThat(generatorTB.checkMessageDontContainData(messages.get(3), myCellsNameAndData)).isFalse();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
    }

    @Test
    public void MY_CELLSWithoutGroupsTest() {
        //Проверяемая команда
        String command = Command.MY_CELLS.getTextCommand();
        //новый пользователь без всего(без групп)
        User user = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chat = user.getChatTelegram();
        String messageCheck = MESSAGE_YOU_DONT_HAVE_GROUP;

        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command, GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(1)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(1);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck);
        //Без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(0))).isTrue();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
    }

    @Test
    public void MY_CELLSWithOneGroupTest() {
        //Проверяемая команда
        String command = Command.MY_CELLS.getTextCommand();
        //новый пользователь
        User user = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chat = user.getChatTelegram();
        Cell cell = generatorEntity.findRandomCell();
        UserAlias userAlias = generatorEntity.generateSaveUserAlias(user, cell);

        String messageCheck = cell.getName() + "\n" + MESSAGE_SELECT_COMMAND;
        TreeSet<Command> commands = commandServiceTB.getCommands(3);
        commands.remove(Command.WARD);
        List<String> listNameAndData = generatorTB.getListNameAndDataPlusStr(commands, userAlias.getId().toString());

        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command, GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(1)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(1);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(0), listNameAndData)).isTrue();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
    }

    @Test
    public void MY_CELLSWithManyGroupsTest() {
        //Проверяемая команда
        String command = Command.MY_CELLS.getTextCommand();
        //новый пользователь
        User user = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chat = user.getChatTelegram();
        UserAlias userAlias1 = generatorEntity.generateSaveUserAlias(user, null);
        UserAlias userAlias2 = generatorEntity.generateSaveUserAlias(user, null);
        Cell cell1 = generatorEntity.generateSaveCell(userAlias1);
        Cell cell2 = generatorEntity.generateSaveCell(userAlias2);
        List<UserAlias> userAliases = new ArrayList<>();
        userAliases.add(generatorEntity.linkCellUserAlias(userAlias1, cell1));
        userAliases.add(generatorEntity.linkCellUserAlias(userAlias2, cell2));

        String messageCheck = MESSAGE_SELECT_GROUP;
        LinkedHashMap<String, String> buttons = new LinkedHashMap<>();
        userAliases.forEach(userAlias -> buttons.put(
                userAlias.getCell().getName(),
                Command.MANAGE_CELL.getTextCommand() + REQUEST_SPLIT_SYMBOL + userAlias.getCell().getId()));
        List<String> listNameAndData = generatorTB.getListNameAndData(buttons);

        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command, GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(1)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(1);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(0), listNameAndData)).isTrue();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
    }

    @Test
    public void MY_DATATest() {
        //Проверяемая команда
        String command = Command.MY_DATA.getTextCommand();
        //новый пользователь без всего
        User user = generatorEntity.findRandomUser();
        Chat chat = user.getChatTelegram();
        UserAlias userAlias = userAliasRepository.findByUser(user.getId()).get(0);
        String messageCheck1 = MESSAGE_FAIL_ARGUMENT;
        String messageCheck2 = MESSAGE_YOU_DONT_HAVE_THIS_GROUP;
        String messageCheck3 = userAlias.getDisplayName() + "\n" + MESSAGE_SELECT_COMMAND;
        long dontExistId = random.nextInt();
        while (userAliasRepository.findById(dontExistId).isPresent()) {
            dontExistId = random.nextInt();
        }
        List<String> listNameAndData = generatorTB.getListNameAndDataPlusStr(
                commandServiceTB.getCommands(4), userAlias.getId().toString());

        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + dontExistId, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + userAlias.getId(), GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(3)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(3);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck1);
        //без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(0))).isTrue();
        //проверяем сообщения 2
        assertThat(messages.get(1).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(1).getParameters().get("text")).isEqualTo(messageCheck2);
        //без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(1))).isTrue();
        //проверяем сообщения 3
        assertThat(messages.get(2).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(2).getParameters().get("text")).isEqualTo(messageCheck3);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(2), listNameAndData)).isTrue();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
    }

    @Test
    public void WARDTest() {
        //Проверяемая команда
        String command = Command.WARD.getTextCommand();
        //новый пользователь без всего
        User user = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chat = user.getChatTelegram();
        Cell cell1 = generatorEntity.findRandomCell();
        Cell cell2 = generatorEntity.findRandomCellWithout(List.of(cell1));
        UserAlias userAlias1 = generatorEntity.generateSaveUserAlias(user, cell1);
        UserAlias userAlias2 = generatorEntity.generateSaveUserAlias(user, cell2);

        //сообщение для сообщения без id
        String messageCheck1 = MESSAGE_FAIL_ARGUMENT;
        //сообщение с несуществующим id
        String messageCheck2 = MESSAGE_YOU_DONT_HAVE_THIS_GROUP;
        //сообщение с нормальным id, но распределения еще нет
        String messageCheck3 = MESSAGE_WARD_NOT_YET.formatted(cell1.getMailingDate().toString());
        List<String> listNameAndData3 = List.of(
                Messages.BUTTON_BACK, Command.MANAGE_CELL.getTextCommand() + REQUEST_SPLIT_SYMBOL + userAlias1.getId());
        //сообщение с нормальным id, с распределением
        generatorEntity.calcSecretSanta(cell2);
        UserAlias recipient = userAliasRepository.findById(userAlias2.getId()).stream()
                .map(UserAlias::getRecipient)
                .findAny().orElse(null);
        assert recipient != null;
        String messageCheck4 = MESSAGE_WARD_SANTA.formatted(recipient.getDisplayName()) +
                               recipient.getDreams();
        List<String> listNameAndData4 = generatorTB.getListNameAndDataPlusStr(
                commandServiceTB.getCommands(5), userAlias2.getId().toString());
        //находим не сущестующий id
        long dontExistId = random.nextInt();
        while (userAliasRepository.findById(dontExistId).isPresent()) {
            dontExistId = random.nextInt();
        }
        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + dontExistId, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + userAlias1.getId(), GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + userAlias2.getId(), GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(4)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(4);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck1);
        //без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(0))).isTrue();
        //проверяем сообщения 2
        assertThat(messages.get(1).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(1).getParameters().get("text")).isEqualTo(messageCheck2);
        //кнопки
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(1))).isTrue();
        //проверяем сообщения 3
        assertThat(messages.get(2).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(2).getParameters().get("text")).isEqualTo(messageCheck3);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(2), listNameAndData3)).isTrue();
        //проверяем размеры репозиториев
        //проверяем сообщения 4
        assertThat(messages.get(3).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(3).getParameters().get("text")).isEqualTo(messageCheck4);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(3), listNameAndData4)).isTrue();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
    }

    @Test
    public void MANAGE_CELLTest() {
        //Проверяемая команда
        String command = Command.MANAGE_CELL.getTextCommand();
        //новый пользователь без всего
        User user = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chat = user.getChatTelegram();
        Cell cell1 = generatorEntity.findRandomCell();
        Cell cell2 = generatorEntity.findRandomCellWithout(Collections.singletonList(cell1));
        UserAlias userAlias1 = generatorEntity.generateSaveUserAlias(user, cell1);
        UserAlias userAlias2 = generatorEntity.generateSaveUserAlias(user, cell2);
        generatorEntity.calcSecretSanta(cell2);
        //сообщение для сообщения без id
        String messageCheck1 = MESSAGE_FAIL_ARGUMENT;
        //сообщение с несуществующим id
        String messageCheck2 = MESSAGE_YOU_DONT_HAVE_THIS_GROUP;
        //сообщение с нормальным id без распределения. удаляем WARD
        String messageCheck3 = cell1.getName() + "\n" + MESSAGE_SELECT_COMMAND;
        TreeSet<Command> commands3 = commandServiceTB.getCommands(3);
        commands3.remove(Command.WARD);
        List<String> listNameAndData3 = generatorTB.getListNameAndData(commands3);
        //сообщение с нормальным id с распределением. с WARD
        String messageCheck4 = cell2.getName() + "\n" + MESSAGE_SELECT_COMMAND;
        TreeSet<Command> commands4 = commandServiceTB.getCommands(3);
        List<String> listNameAndData4 = generatorTB.getListNameAndData(commands4);
        //находим не сущестующий id
        long dontExistId = random.nextInt();
        while (userAliasRepository.findById(dontExistId).isPresent()) {
            dontExistId = random.nextInt();
        }
        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + dontExistId, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + userAlias1.getId(), GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + userAlias2.getId(), GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(4)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(4);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck1);
        //без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(0))).isTrue();
        //проверяем сообщения 2
        assertThat(messages.get(1).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(1).getParameters().get("text")).isEqualTo(messageCheck2);
        //кнопки
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(1))).isTrue();
        //проверяем сообщения 3
        assertThat(messages.get(2).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(2).getParameters().get("text")).isEqualTo(messageCheck3);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(2), listNameAndData3)).isTrue();
        //проверяем размеры репозиториев
        //проверяем сообщения 4
        assertThat(messages.get(3).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(3).getParameters().get("text")).isEqualTo(messageCheck4);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(3), listNameAndData4)).isTrue();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
    }
    @Test
    public void MENU_BACK4Test() {
        //Проверяемая команда
        String command = Command.MENU_BACK4.getTextCommand();
        //новый пользователь без всего
        User user = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chat = user.getChatTelegram();
        Cell cell1 = generatorEntity.findRandomCell();
        Cell cell2 = generatorEntity.findRandomCellWithout(Collections.singletonList(cell1));
        UserAlias userAlias1 = generatorEntity.generateSaveUserAlias(user, cell1);
        UserAlias userAlias2 = generatorEntity.generateSaveUserAlias(user, cell2);
        generatorEntity.calcSecretSanta(cell2);
        //сообщение для сообщения без id
        String messageCheck1 = MESSAGE_FAIL_ARGUMENT;
        //сообщение с несуществующим id
        String messageCheck2 = MESSAGE_YOU_DONT_HAVE_THIS_GROUP;
        //сообщение с нормальным id без распределения. удаляем WARD
        String messageCheck3 = cell1.getName() + "\n" + MESSAGE_SELECT_COMMAND;
        TreeSet<Command> commands3 = commandServiceTB.getCommands(3);
        commands3.remove(Command.WARD);
        List<String> listNameAndData3 = generatorTB.getListNameAndData(commands3);
        //сообщение с нормальным id с распределением. с WARD
        String messageCheck4 = cell2.getName() + "\n" + MESSAGE_SELECT_COMMAND;
        TreeSet<Command> commands4 = commandServiceTB.getCommands(3);
        List<String> listNameAndData4 = generatorTB.getListNameAndData(commands4);
        //находим не сущестующий id
        long dontExistId = random.nextInt();
        while (userAliasRepository.findById(dontExistId).isPresent()) {
            dontExistId = random.nextInt();
        }
        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + dontExistId, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + userAlias1.getId(), GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + userAlias2.getId(), GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(4)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(4);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck1);
        //без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(0))).isTrue();
        //проверяем сообщения 2
        assertThat(messages.get(1).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(1).getParameters().get("text")).isEqualTo(messageCheck2);
        //кнопки
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(1))).isTrue();
        //проверяем сообщения 3
        assertThat(messages.get(2).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(2).getParameters().get("text")).isEqualTo(messageCheck3);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(2), listNameAndData3)).isTrue();
        //проверяем размеры репозиториев
        //проверяем сообщения 4
        assertThat(messages.get(3).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(3).getParameters().get("text")).isEqualTo(messageCheck4);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(3), listNameAndData4)).isTrue();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
    }
    @Test
    public void MENU_BACK5Test() {
        //Проверяемая команда
        String command = Command.MENU_BACK5.getTextCommand();
        //новый пользователь без всего
        User user = generatorEntity.generateAndSaveNewUserWithChat();
        Chat chat = user.getChatTelegram();
        Cell cell1 = generatorEntity.findRandomCell();
        Cell cell2 = generatorEntity.findRandomCellWithout(Collections.singletonList(cell1));
        UserAlias userAlias1 = generatorEntity.generateSaveUserAlias(user, cell1);
        UserAlias userAlias2 = generatorEntity.generateSaveUserAlias(user, cell2);
        generatorEntity.calcSecretSanta(cell2);
        //сообщение для сообщения без id
        String messageCheck1 = MESSAGE_FAIL_ARGUMENT;
        //сообщение с несуществующим id
        String messageCheck2 = MESSAGE_YOU_DONT_HAVE_THIS_GROUP;
        //сообщение с нормальным id без распределения. удаляем WARD
        String messageCheck3 = cell1.getName() + "\n" + MESSAGE_SELECT_COMMAND;
        TreeSet<Command> commands3 = commandServiceTB.getCommands(3);
        commands3.remove(Command.WARD);
        List<String> listNameAndData3 = generatorTB.getListNameAndData(commands3);
        //сообщение с нормальным id с распределением. с WARD
        String messageCheck4 = cell2.getName() + "\n" + MESSAGE_SELECT_COMMAND;
        TreeSet<Command> commands4 = commandServiceTB.getCommands(3);
        List<String> listNameAndData4 = generatorTB.getListNameAndData(commands4);
        //находим не сущестующий id
        long dontExistId = random.nextInt();
        while (userAliasRepository.findById(dontExistId).isPresent()) {
            dontExistId = random.nextInt();
        }
        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        telegramBotUpdatesListener.process(List.of(
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + dontExistId, GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + userAlias1.getId(), GeneratorTB.UpdateData.MESSAGE),
                generatorTB.getUpdate(generatorTB.mapToUser(chat), command + REQUEST_SPLIT_SYMBOL + userAlias2.getId(), GeneratorTB.UpdateData.MESSAGE)));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(4)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(4);

        //проверяем сообщения 1
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck1);
        //без кнопок
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(0))).isTrue();
        //проверяем сообщения 2
        assertThat(messages.get(1).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(1).getParameters().get("text")).isEqualTo(messageCheck2);
        //кнопки
        assertThat(generatorTB.checkMessageDontContainButton(messages.get(1))).isTrue();
        //проверяем сообщения 3
        assertThat(messages.get(2).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(2).getParameters().get("text")).isEqualTo(messageCheck3);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(2), listNameAndData3)).isTrue();
        //проверяем размеры репозиториев
        //проверяем сообщения 4
        assertThat(messages.get(3).getParameters().get("chat_id")).isEqualTo(chat.getId());
        assertThat(messages.get(3).getParameters().get("text")).isEqualTo(messageCheck4);
        //кнопки
        assertThat(generatorTB.checkMessageContainData(messages.get(3), listNameAndData4)).isTrue();
        //проверяем размеры репозиториев
        generatorEntity.checkRepositoryState(repositoryState, null);
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
