package secretSanta.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import secretSanta.entity.Cell;
import secretSanta.entity.Chat;
import secretSanta.entity.User;
import secretSanta.entity.UserAlias;
import secretSanta.entityDto.ChatDto;
import secretSanta.exception.ChatNotFoundException;
import secretSanta.mapper.ChatMapper;
import secretSanta.repository.CellRepository;
import secretSanta.repository.ChatRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The class is designed for the operation of the Call Request Service class
 * An important parameter that affects the operation of the CALL_REQUEST CALL_CLIENT command.
 */
@Service
public class CellService {
    private final CellRepository cellRepository;

    public CellService(CellRepository cellRepository) {
        this.cellRepository = cellRepository;
    }

    public Cell addCell(Cell cell) {
        if (cell.getId() != null) {
            throw new IllegalArgumentException("cell.Id must be null");
        }
        return cellRepository.save(cell);
    }

    public Cell lockCell(Cell cell) {
        if (cell.getId() == null) {
            throw new IllegalArgumentException("cell.Id must be not null");
        }
        cell.setLock(true);
        return cellRepository.save(cell);
    }

    public List<Cell> findUnlockCellForMailing() {
        return cellRepository.findUnlockCellForMailing();
    }

    public Cell findByTokenDB(String tokenDB) {
        return cellRepository.findByTokenDB(tokenDB).orElse(null);
    }

    public boolean existByTokenDB(String tokenDB) {
        return cellRepository.countByTokenDB(tokenDB) > 0;
    }

    public Cell findByUserAlias(Long userAliasId) {
        return cellRepository.findByUserAlias(userAliasId).orElse(null);
    }

    public boolean exist(User user, Cell cell) {
        return cellRepository.count(user.getId(), cell.getId()) > 0;
    }

    public int countUserAliases(Cell cell) {
        return cellRepository.countUserAliases(cell.getId());
    }

    public List<UserAlias> getUserAliases(Cell cell) {
        return cellRepository.getUserAliases(cell.getId());
    }


}
