package secretSanta.service;

import org.springframework.stereotype.Service;
import secretSanta.entity.Cell;
import secretSanta.entity.User;
import secretSanta.entity.UserAlias;
import secretSanta.repository.CellRepository;
import secretSanta.repository.UserAliasRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The class is designed for the operation of the Call Request Service class
 * An important parameter that affects the operation of the CALL_REQUEST CALL_CLIENT command.
 */
@Service
public class UserAliasService {
    private final UserAliasRepository userAliasRepository;

    public UserAliasService(UserAliasRepository userAliasRepository) {
        this.userAliasRepository = userAliasRepository;
    }

    public UserAlias addUserAlias(UserAlias userAlias) {
        if (userAlias.getId() != null) {
            throw new IllegalArgumentException("userAlias.Id must be null");
        }
        return userAliasRepository.save(userAlias);
    }

    public UserAlias update(UserAlias userAlias) {
        if (userAlias.getId() == null) {
            throw new IllegalArgumentException("userAlias.Id must not be null");
        }
        return userAliasRepository.save(userAlias);
    }

    public List<UserAlias> updateAll(List<UserAlias> userAliases) {
        userAliases.forEach(userAlias -> {
            if (userAlias.getId() == null) {
                throw new IllegalArgumentException("userAlias.Id must not be null");
            }
        });
        return userAliasRepository.saveAll(userAliases);
    }

    public void delete(UserAlias userAlias) {
        if (userAlias.getId() == null) {
            throw new IllegalArgumentException("userAlias.Id must not be null");
        }
        userAliasRepository.delete(userAlias);
    }

    public UserAlias disable(UserAlias userAlias) {
        if (userAlias.getId() == null) {
            throw new IllegalArgumentException("userAlias.Id must not be null");
        }
        userAlias.setEnable(false);
        return update(userAlias);
    }

    public UserAlias enable(UserAlias userAlias) {
        if (userAlias.getId() == null) {
            throw new IllegalArgumentException("userAlias.Id must not be null");
        }
        userAlias.setEnable(true);
        return update(userAlias);
    }

    public int countUserAliases(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("user.Id must not be null");
        }
        return userAliasRepository.countUserAliases(user.getId());
    }

    public List<UserAlias> findByUser(User user) {
        if (user != null) {
            return userAliasRepository.findByUser(user.getId());
        }
        return new ArrayList<>();
    }

    public Optional<UserAlias> findByUserAndCell(User user, Cell cell) {
        if (user != null) {
            return userAliasRepository.findByUserAndCell(user.getId(), cell.getId());
        }
        return Optional.empty();
    }

    public Optional<UserAlias> findByUserAndCellIncludeDeleted(User user, Cell cell) {
        if (user != null) {
            return userAliasRepository.findByUserAndCellIncludeDeleted(user.getId(), cell.getId());
        }
        return Optional.empty();
    }

    public Optional<UserAlias> findById(Long userAliasId) {
        if (userAliasId != null) {
            return userAliasRepository.findById(userAliasId);
        }
        return Optional.empty();
    }

    public Optional<UserAlias> findByRecipient(UserAlias recipient) {
        if (recipient.getId() != null) {
            return userAliasRepository.findByRecipient(recipient.getId());
        }
        return Optional.empty();
    }
}
