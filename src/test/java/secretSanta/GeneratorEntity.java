package secretSanta;

import com.github.javafaker.Faker;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import secretSanta.entity.*;
import secretSanta.repository.*;
import secretSanta.security.EncoderService;
import secretSanta.security.Roles;
import secretSanta.service.Telegram.UpdateMapperTB;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
@Profile("testGenerateData")
public class GeneratorEntity {
    public enum NameRepo {
        AUTHORITY_REPOSITORY,
        CELL_REPOSITORY,
        CHAT_REPOSITORY,
        ONGOINGREQUEST_TB_REPOSITORY,
        USER_ALIAS_REPOSITORY,
        USER_REPOSITORY
    }

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
    private EncoderService encoderService;
    private final Faker faker = new Faker();
    private final Random random = new Random();

    public List<UserAlias> calcSecretSanta(Cell cell) {
        //ставим блок
        cell.setLock(true);
        cell = cellRepository.save(cell);
        //берем алиасы
        List<UserAlias> userAliasesSanta = cellRepository.getUserAliases(cell.getId());
        if (userAliasesSanta.isEmpty()) {
            throw new IllegalArgumentException("userAliasesSanta.isEmpty()");
        }
        //распределяем без рандома
        //если один, то сам у себя
        if (userAliasesSanta.size() == 1) {
            UserAlias userAlias = userAliasesSanta.get(0);
            userAlias.setRecipient(userAlias);
            userAlias = userAliasRepository.save(userAlias);
            return List.of(userAlias);
        }
        //если много, то последовательно
        for (int i = 0; i < userAliasesSanta.size(); i++) {
            UserAlias santa = userAliasesSanta.get(i);
            UserAlias recipient;
            if (i + 1 < userAliasesSanta.size()) {
                recipient = userAliasesSanta.get(i + 1);
            } else {
                recipient = userAliasesSanta.get(0);
            }
            userAliasesSanta.get(i).setRecipient(recipient);
            userAliasRepository.save(userAliasesSanta.get(i));
        }
        return userAliasRepository.findByCell(cell.getId());
    }

    public Cell setOnlyOneCellMailing() {
        List<Cell> cells = cellRepository.findAll();
        if (cells.isEmpty()) {
            throw new IllegalArgumentException("cellRepository isEmpty");
        }
        Cell result = cells.get(0);
        result.setLock(false);
        result.setCreateDate(LocalDate.now().minusDays(7));
        result.setMailingDate(LocalDate.now());
        result.setEndDate(LocalDate.now().plusMonths(1));
        if (cells.size() > 1) {
            for (int i = 1; i < cells.size(); i++) {
                Cell cell = cells.get(i);
                cell.setLock(true);
                cell.setCreateDate(LocalDate.now().minusDays(7));
                cell.setMailingDate(LocalDate.now().plusMonths(1));
                cell.setEndDate(LocalDate.now().plusMonths(2));
            }
        }
        cellRepository.saveAll(cells);
        return cellRepository.findById(result.getId())
                .orElseThrow(() -> new RuntimeException("result cell == null"));
    }

    public boolean existChat(Chat chat) {
        Chat chatFrom = chatRepository.findById(chat.getId()).orElse(null);
        if (chatFrom == null) {
            return false;
        }
        return chatFrom.getId().equals(chat.getId()) &&
                chatFrom.getFirstNameUser().equals(chat.getFirstNameUser()) &&
                chatFrom.getLastNameUser().equals(chat.getLastNameUser()) &&
                chatFrom.getUserNameTelegram().equals(chat.getUserNameTelegram());
    }

    public boolean existUserWithChat(Chat chat) {
        return userRepository.getByIdChat(chat.getId()).isPresent();
    }

    public boolean existUser(User user) {
        User userFrom = userRepository.findById(user.getId()).orElse(null);
        if (userFrom == null) {
            return false;
        }
        return userFrom.getId().equals(user.getId()) &&
                userFrom.getEnabled().equals(user.getEnabled()) &&
                userFrom.getDisplayName().equals(user.getDisplayName()) &&
                userFrom.getAliases().equals(user.getAliases()) &&
                userFrom.getChatTelegram().equals(user.getChatTelegram()) &&
                userFrom.getUsername().equals(user.getUsername()) &&
                userFrom.getAuthorities().equals(user.getAuthorities());
    }

    public Map<String, Integer> getRepositoryState() {
        Map<String, Integer> result = new HashMap<>();
        result.put(NameRepo.AUTHORITY_REPOSITORY.name(), authorityRepository.findAll().size());
        result.put(NameRepo.CELL_REPOSITORY.name(), cellRepository.findAll().size());
        result.put(NameRepo.CHAT_REPOSITORY.name(), chatRepository.findAll().size());
        result.put(NameRepo.ONGOINGREQUEST_TB_REPOSITORY.name(), ongoingRequestTBRepository.findAll().size());
        result.put(NameRepo.USER_ALIAS_REPOSITORY.name(), userAliasRepository.findAll().size());
        result.put(NameRepo.USER_REPOSITORY.name(), userRepository.findAll().size());
        return result;
    }

    public boolean checkRepositoryState(Map<String, Integer> before, Map<String, Integer> changes) {
        Map<String, Integer> result = getRepositoryState();
        for (Map.Entry<String, Integer> after : result.entrySet()) {
            String afterKey = after.getKey();
            if (!before.containsKey(afterKey)) {
                System.err.println("absent key in Before map : " + afterKey);
                return false;
            }
            int afterInt = after.getValue();
            int beforeInt = before.get(afterKey);
            int beforeIntCalc = beforeInt;
            if (changes != null && changes.containsKey(afterKey)) {
                beforeIntCalc = beforeIntCalc + changes.get(afterKey);
            }
            if (beforeIntCalc != afterInt) {
                System.err.println("not equals values in " + afterKey + " \"before\": " + beforeInt + " | \"after\": " + afterInt);
                if (changes != null && changes.containsKey(afterKey)) {
                    System.err.println("\"after\" should be changed like this " + changes.get(afterKey));
                }
                return false;
            }
        }
        return true;
    }


    public void clearData() {
        ongoingRequestTBRepository.deleteAll();
        chatRepository.deleteAll();
        userAliasRepository.findAll().forEach(
                userAlias -> {
                    userAlias.setCell(null);
                    userAliasRepository.save(userAlias);
                });
        cellRepository.deleteAll();
        userAliasRepository.deleteAll();
        authorityRepository.deleteAll();
        userRepository.deleteAll();
    }

    public Chat generateNewChatWithoutSave() {
        return getChat(
                generateIdTelegram(),
                generateUsername(),
                generateFirstName(),
                generateLastName(),
                null,
                null,
                null);
    }


    public User generateAndSaveNewUserWithChat() {
        String username = generateUsername();
        User user = getUser(
                generateId(),
                generateFirstName() + " " + generateLastName(),
                null,
                null,
                generateDate(false, LocalDate.now()),
                generatePass(),
                username,
                true,
                null);
        user = userRepository.save(user);
        Authority authority = new Authority();
        authority.setAuthority(Roles.CLIENT);
        authority.setUser(user);
        authorityRepository.save(authority);
        Chat chat = getChat(
                generateIdTelegram(),
                username,
                generateFirstName(),
                generateLastName(),
                generateDateTime(false, LocalDateTime.now()),
                user,
                null);
        chatRepository.save(chat);
        return userRepository.findById(user.getId()).orElse(null);
    }

    public User generateAndSaveNewUser() {
        User user = userRepository.save(
                getUser(
                        generateId(),
                        generateFirstName() + " " + generateLastName(),
                        null,
                        null,
                        generateDate(false, LocalDate.now()),
                        generatePass(),
                        generateUsername(),
                        true,
                        null));
        Authority authority = new Authority();
        authority.setAuthority(Roles.CLIENT);
        authority.setUser(user);
        authorityRepository.save(authority);
        return userRepository.findById(user.getId()).orElse(null);
    }

    public User findRandomUser() {
        List<User> users = userRepository.findAll();
        return users.get(random.nextInt(users.size()));
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public int countUsers() {
        return userRepository.findAll().size();
    }

    public Cell findRandomCell() {
        List<Cell> cells = cellRepository.findAll();
        return cells.get(random.nextInt(cells.size()));
    }

    public Cell findRandomCellWithout(List<Cell> cells) {
        List<Cell> cellAll = cellRepository.findAll();
        cells.forEach(cellAll::remove);
        if (cellAll.isEmpty()) {
            return null;
        }
        return cellAll.get(random.nextInt(cellAll.size()));
    }

    public Chat findRandomChat() {
        List<Chat> chats = chatRepository.findAll();
        return chats.get(random.nextInt(chats.size()));
    }

    public UserAlias findRandomUserAlias() {
        List<UserAlias> userAliases = userAliasRepository.findAll();
        return userAliases.get(random.nextInt(userAliases.size()));
    }

    public UserAlias linkCellUserAlias(UserAlias userAlias, Cell cell) {
        userAlias.setCell(cell);
        return userAliasRepository.save(userAlias);
    }

    public UserAlias generateSaveUserAlias(User user, Cell cell) {
        UserAlias userAlias = new UserAlias();
        userAlias.setDisplayName(generateFirstName());
        userAlias.setDreams(generateDream());
        userAlias.setCell(cell);
        userAlias.setUser(user);
        userAlias.setEnable(true);
        userAlias.setRequestDreamRecipient(false);
        userAlias.setRecipient(null);
        return userAliasRepository.save(userAlias);
    }

    public Cell generateSaveCell(UserAlias userAlias) {
        String token = encoderService.generateToken();
        String hash = encoderService.getHash(token);
        Cell cell = new Cell();
        cell.setName(generateDream());
        cell.setOwner(userAlias);
        cell.setCreateDate(generateDate(false, LocalDate.now()));
        cell.setMailingDate(generateDate(true, LocalDate.now()));
        cell.setEndDate(generateDate(true, cell.getMailingDate()));
        cell.setLock(false);
        cell.setTokenDB(encoderService.getTokenDB(token));
        cell.setTokenHash(hash);
        return cellRepository.save(cell);
    }

    public void generateData(int userInt) {
        if (userInt < 3) {
            throw new IllegalArgumentException("userInt must be > 2");
        }
        int cellInt = userInt / 3;
        int userAliasIntMin = userInt / (cellInt + 1); //must be < chatsInt / userInt
        int userAliasIntMax = userInt / (cellInt - 1); //must be > chatsInt / userInt

        //generate chat & user
        for (int i = 0; i < userInt; i++) {
            String username = generateUsername();
            User user = getUser(
                    generateId(),
                    generateFirstName() + " " + generateLastName(),
                    null,
                    null,
                    generateDate(false, LocalDate.now()),
                    generatePass(),
                    username,
                    true,
                    null);
            user = userRepository.save(user);
            Authority authority = new Authority();
            authority.setAuthority(Roles.CLIENT);
            authority.setUser(user);
            authorityRepository.save(authority);
            Chat chat = getChat(
                    generateIdTelegram(),
                    username,
                    generateFirstName(),
                    generateLastName(),
                    generateDateTime(false, LocalDateTime.now()),
                    user,
                    null);
            chatRepository.save(chat);
        }
        //generate cell with userAlias=owner
        for (int i = 0; i < cellInt; i++) {
            String token = encoderService.generateToken();
            String hash = encoderService.getHash(token);
            UserAlias userAlias = new UserAlias();
            userAlias.setDisplayName(generateFirstName());
            userAlias.setDreams(faker.backToTheFuture().character());
            userAlias.setUser(findRandomUser());
//            userAlias.setCell(cell);
            userAlias = userAliasRepository.save(userAlias);
            Cell cell = new Cell();
            cell.setName(generateDream());
            cell.setOwner(userAlias);
            cell.setCreateDate(generateDate(false, LocalDate.now()));
            cell.setMailingDate(generateDate(true, LocalDate.now()));
            cell.setEndDate(generateDate(true, cell.getMailingDate()));
            cell.setLock(false);
            cell.setTokenDB(encoderService.getTokenDB(token));
            cell.setTokenHash(hash);
            cell = cellRepository.save(cell);
            userAlias.setCell(cell);
            userAlias = userAliasRepository.save(userAlias);
        }
        //generate userAliases
        List<UserAlias> userAliases = userAliasRepository.findAll();
        for (User user : getUsers()) {
            List<Cell> cells = cellRepository.findAll();
            int count = genInt(userAliasIntMin, userAliasIntMax);
            for (int i = 0; i < count; i++) {
                if (!cells.isEmpty()) {
                    Cell randomCell = cells.get(random.nextInt(cells.size()));
                    if (userAliases.stream()
                            .map(ua -> ua.getCell().getId())
                            .filter(id -> id.equals(randomCell.getId()))
                            .findAny().isEmpty() ||
                            userAliases.stream()
                                    .map(ua -> ua.getUser().getId())
                                    .filter(id -> id.equals(user.getId()))
                                    .findAny().isEmpty()) {
                        UserAlias userAlias = new UserAlias();
                        userAlias.setDisplayName(generateFirstName());
                        userAlias.setDreams(generateDream());
                        userAlias.setRecipient(null);
                        userAlias.setCell(randomCell);
                        userAlias.setUser(user);
                        userAlias.setEnable(true);
                        userAliasRepository.save(userAlias);
                    }
                    cells.remove(randomCell);
                }
            }
        }
    }

    public String generatePass() {
        return faker.internet().password();
    }

    public LocalDate generateDate(boolean isAfter, LocalDate localDate) {
        LocalDate tld = LocalDate.now();
        int year = tld.getYear();
        if (isAfter) {
            tld = localDate.plusYears(1L);
            while (tld.isBefore(localDate)) {
                tld = LocalDate.of(genInt(year - 2, year), genInt(12), genInt(25));
            }
        } else {
            tld = localDate.minusYears(1L);
            while (tld.isAfter(localDate)) {
                tld = LocalDate.of(genInt(year - 2, year), genInt(12), genInt(25));
            }
        }
        return tld;
    }

    public Chat mapUpdateToChat(Update update, User user) {
        Chat chat = new Chat();
        if (update.message() != null) {
            chat.setId(UpdateMapperTB.getIdChat(update));
            chat.setFirstNameUser(UpdateMapperTB.getFirstName(update));
            chat.setLastNameUser(UpdateMapperTB.getLastName(update));
            chat.setUserNameTelegram(UpdateMapperTB.getUsername(update));
            chat.setUser(user);
        } else if (update.callbackQuery() != null) {
            chat.setId(UpdateMapperTB.getIdChat(update));
            chat.setFirstNameUser(UpdateMapperTB.getFirstName(update));
            chat.setLastNameUser(UpdateMapperTB.getLastName(update));
            chat.setUserNameTelegram(UpdateMapperTB.getUsername(update));
            chat.setUser(user);
        }
        return chat;
    }

    public User getUser(
            Long idUser,
            String displayName,
            Chat chatTelegram,
            List<Authority> authorities,
            LocalDate regDate,
            String password,
            String username,
            Boolean enabled,
            List<UserAlias> aliases) {
        User user = new User();
        user.setId(idUser);
        user.setDisplayName(displayName);
        user.setChatTelegram(chatTelegram);
        user.setAuthorities(authorities);
        user.setRegDate(regDate);
        user.setPassword(password);
        user.setUsername(username);
        user.setEnabled(enabled);
        user.setAliases(aliases);
        return user;
    }

    public Chat getChat(Long idChat,
                        String userNameTelegram,
                        String firstNameUser,
                        String lastNameUser,
                        LocalDateTime last_activity,
                        User user,
                        OngoingRequestTB ongoingRequestTB) {
        Chat chat = new Chat();
        chat.setId(idChat);
        chat.setUserNameTelegram(userNameTelegram);
        chat.setFirstNameUser(firstNameUser);
        chat.setLastNameUser(lastNameUser);
        chat.setLastActivity(last_activity);
        chat.setUser(user);
        chat.setOngoingRequestTB(ongoingRequestTB);
        return chat;
    }

    public String generateUsername() {
        return faker.name().username();
    }

    public String generateFirstName() {
        return faker.name().firstName();
    }

    public String generateLastName() {
        return faker.name().lastName();
    }

    public Long generateIdTelegram() {
        long idTemp = -1L;
        //id with <100 I leave for my needs
        while (idTemp < 100) {
            idTemp = faker.random().nextLong(999_999_999 - 100_000_000) + 100_000_000;
        }
        return idTemp;
    }

    public Long generateId() {
        return faker.random().nextLong();
    }

    public String generateDream() {
        return faker.animal().name();
    }

    public String generateMessage() {
        return faker.lordOfTheRings().character();
    }

    public boolean generateBool() {
        return faker.bool().bool();
    }

    public Boolean generateBoolWithNull() {
        int i = random.nextInt(50);
        if (i < 25) {
            return faker.bool().bool();
        } else {
            return null;
        }
    }

    public int genInt(int max) {
        return random.nextInt(max);
    }

    public int genInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public byte[] genBytes() {
        return faker.avatar().image().getBytes();
    }


    public LocalDateTime generateDateTime(boolean isAfter, LocalDateTime localDateTime) {
        LocalDateTime tldt = LocalDateTime.now();
        int year = tldt.getYear();
        if (isAfter) {
            tldt = localDateTime.plusYears(1L);
            while (tldt.isBefore(localDateTime)) {
                LocalDate localDate = LocalDate.of(genInt(year - 2, year), genInt(12), genInt(25));
                LocalTime localTime = LocalTime.of(genInt(23), genInt(59), genInt(59), 0);
                tldt = LocalDateTime.of(localDate, localTime);
            }
        } else {
            tldt = localDateTime.minusYears(1L);
            while (tldt.isAfter(localDateTime)) {
                LocalDate localDate = LocalDate.of(genInt(year - 2, year), genInt(12), genInt(25));
                LocalTime localTime = LocalTime.of(genInt(23), genInt(59), genInt(59), 0);
                tldt = LocalDateTime.of(localDate, localTime);
            }
        }
        return tldt;
    }
}
