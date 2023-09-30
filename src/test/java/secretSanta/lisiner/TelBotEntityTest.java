package secretSanta.lisiner;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
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
import secretSanta.service.*;
import secretSanta.service.Telegram.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("testGenerateData")
public class TelBotEntityTest {

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
    private SecretSantaService secretSantaService;
    @Autowired
    @InjectMocks
    private TelegramBotUpdatesListener telegramBotUpdatesListener;
    private final GeneratorTB generatorTB = new GeneratorTB();
    private final Random random = new Random();

    @Test
    public void createNewUserAndChatTest() {
        String command = Command.START.getTextCommand();
        //новый чат без всего
        Chat chatNew = generatorEntity.generateNewChatWithoutSave();

        //запоминаем размеры репозиториев
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();
        //проверяем что в системе нет таких пользователей
        assertThat(generatorEntity.existChat(chatNew)).isFalse();
        assertThat(generatorEntity.existUserWithChat(chatNew)).isFalse();

        Update update = generatorTB.getUpdate(generatorTB.mapToUser(chatNew), command, GeneratorTB.UpdateData.MESSAGE);
        telegramBotUpdatesListener.process(List.of(update));
        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);

        Mockito.verify(telegramBot, times(2)).execute(argumentCaptor.capture());
        List<SendMessage> messages = argumentCaptor.getAllValues();
        assertThat(messages.size()).isEqualTo(2);

        //проверяем сообщения
        String messageCheck1 = Messages.MESSAGE_HELLO + UpdateMapperTB.getDisplayName(update) + ".\n";
        String messageCheck2 = Messages.MESSAGE_SELECT_COMMAND;
        assertThat(messages.get(0).getParameters().get("chat_id")).isEqualTo(chatNew.getId());
        assertThat(messages.get(0).getParameters().get("text")).isEqualTo(messageCheck1);
        assertThat(messages.get(1).getParameters().get("chat_id")).isEqualTo(chatNew.getId());
        assertThat(messages.get(1).getParameters().get("text")).isEqualTo(messageCheck2);
        //проверяем размеры репозиториев
        assertThat(
                generatorEntity.checkRepositoryState(repositoryState, Map.of(
                        GeneratorEntity.NameRepo.CHAT_REPOSITORY.name(), +1,
                        GeneratorEntity.NameRepo.USER_REPOSITORY.name(), +1
//                        GeneratorEntity.NameRepo.AUTHORITY_REPOSITORY.name(), +1  //todo
                ))).isTrue();
        //проверяем что в системе появились чат и пользователь
        assertThat(generatorEntity.existChat(chatNew)).isTrue();
        assertThat(generatorEntity.existUserWithChat(chatNew)).isTrue();
    }


    @Test
    public void calcSecretSantaTest() {
        Cell randomCell = generatorEntity.findRandomCell();
        assertThat(randomCell.getLock()).isFalse();
        List<UserAlias> userAliases = cellRepository.getUserAliases(randomCell.getId());
        int sizeCell = userAliases.size();
        assertThat(sizeCell > 0).isTrue();
        Map<String, Integer> repositoryState = generatorEntity.getRepositoryState();

        secretSantaService.calcSecretSanta(randomCell);

        randomCell = cellRepository.findById(randomCell.getId()).orElse(null);
        assertThat(randomCell).isNotNull();
        assert randomCell != null;
        assertThat(randomCell.getLock()).isTrue();
        assertThat(generatorEntity.checkRepositoryState(repositoryState, null)).isTrue();
        List<UserAlias> santaList = userAliasRepository.findByCell(randomCell.getId());
        if (santaList.size() == 1) {
            assertThat(santaList.get(0).getId().equals(santaList.get(0).getRecipient().getId())).isTrue();
        } else {
            for (UserAlias santa : santaList) {
                assertThat(santa.getId().equals(santa.getRecipient().getId())).isFalse();
            }
        }
    }


    @Test
    void contextLoads() {
        assertThat(telegramBot).isNotNull();
        assertThat(authorityRepository).isNotNull();
        assertThat(cellRepository).isNotNull();
        assertThat(chatRepository).isNotNull();
        assertThat(ongoingRequestTBRepository).isNotNull();
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
