package secretSanta.service;

import org.springframework.stereotype.Service;
import secretSanta.entity.Cell;
import secretSanta.entity.UserAlias;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class SecretSantaService {
    private final CellService cellService;
    private final UserAliasService userAliasService;
    private final Random random = new Random();

    public SecretSantaService(CellService cellService, UserAliasService userAliasService) {
        this.cellService = cellService;
        this.userAliasService = userAliasService;
    }

    public List<UserAlias> calcSecretSanta(Cell cell) {
        cell = cellService.lockCell(cell);
        List<UserAlias> userAliases = cellService.getUserAliases(cell);
        if (userAliases.isEmpty()) {
            throw new IllegalArgumentException("userAliases.isEmpty()");
        }
        if (userAliases.size() == 1) {
            UserAlias userAlias = userAliases.get(0);
            userAlias.setRecipient(userAlias);
            userAlias = userAliasService.update(userAlias);
            return List.of(userAlias);
        }
        List<UserAlias> recipients = new ArrayList<>(userAliases);
        for (int i = 0; i < userAliases.size(); i++) {
            int size = recipients.size();
            int randomInt = random.nextInt(size);
            UserAlias recipient = recipients.get(randomInt);
            if (recipient.getId().equals(userAliases.get(i).getId())) {
                if (userAliases.size() - recipients.size() <= 1) {
                    randomInt = randomInt + 1 < size ? randomInt + 1 : randomInt - 1;
                    recipient = recipients.get(randomInt);
                } else {
                    int randomChange = random.nextInt(userAliases.size() - 1);
                    UserAlias santaRandom = userAliases.get(randomChange);
                    recipient = santaRandom.getRecipient();
                    santaRandom.setRecipient(userAliases.get(i));
                }
            }
            userAliases.get(i).setRecipient(recipient);
            recipients.remove(recipient);
        }
        return userAliasService.updateAll(userAliases);
    }

    public UserAlias getSantaOfRecipient(UserAlias recipient) {
        return userAliasService.findByRecipient(recipient).orElse(null);
    }
}
