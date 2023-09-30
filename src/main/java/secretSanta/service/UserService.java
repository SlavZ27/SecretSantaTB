package secretSanta.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import secretSanta.security.EncoderService;
import secretSanta.security.Roles;
import secretSanta.entity.Authority;
import secretSanta.entity.User;
import secretSanta.entityDto.RegisterReqDto;
import secretSanta.entityDto.UserDto;
import secretSanta.exception.UserNotFoundException;
import secretSanta.mapper.UserMapper;
import secretSanta.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserRepository usersRepository;
    private final AuthorityService authorityService;
    private final UserMapper userMapper;
    private final EncoderService encoderService;

    public UserService(UserRepository usersRepository,
                       AuthorityService authorityService,
                       UserMapper userMapper,
                       EncoderService encoderService) {
        this.usersRepository = usersRepository;
        this.authorityService = authorityService;
        this.userMapper = userMapper;
        this.encoderService = encoderService;
    }

    /**
     * Allows to get user by username.
     *
     * @param username the username
     * @return {@link UserDto}
     */
    public UserDto getUserDto(String username) {
        return userMapper.userToDto(getUserByUsername(username));
    }

    /**
     * Allows to get user by username.
     *
     * @param username the username
     * @return {@link User}
     * @throws UserNotFoundException if passed non-existent username
     */
    public User getUserByUsername(String username) {
        return usersRepository.getByUsername(username).
                orElseThrow(() -> new UserNotFoundException(username));
    }
    public boolean existUsername(String username) {
        return usersRepository.existsByUsername(username);
    }

    public User getUserByIdTelegramChat(Long idChat) {
        return usersRepository.getByIdChat(idChat).
                orElseThrow(() -> new UserNotFoundException(idChat));
    }

    /**
     * Allows to create new user and save it to repository
     *
     * @param registerReq the register req
     * @param pass        the pass
     * @return the pair - user, authority
     */
    public User addUser(RegisterReqDto registerReq, String pass) {
        User user = userMapper.registerReqToUser(registerReq, pass);
        user = usersRepository.save(user);
        return addUser(user);
    }

    public User addUser(User user) {
        user.setId(null);
        user.setRegDate(LocalDate.now());
        user.setEnabled(true);
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(encoderService.generateHash());
        }
        if (user.getUsername() == null) {
            user.setUsername(user.getDisplayName().trim());  //todo translit or chatId
        }
        Authority authority = new Authority();
        authority.setUser(user);
        authority.setAuthority(Roles.CLIENT);
        List<Authority> authorities = new ArrayList<>();
        authorities.add(authority);
        user.setAuthorities(authorities);
        user = usersRepository.save(user);
        return user;
    }

    public User changeDisplayNameUser(User user, String displayName) {
        User oldUser = getUserByUsername(user.getUsername());
        if (displayName != null && !displayName.isEmpty()) {
//            oldUser.setDisplayName(displayName);
        }
        return usersRepository.save(oldUser);
    }

    public boolean setPass(User user, String pass) {
        User userOld = usersRepository.getByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException(user.getUsername()));
        userOld.setPassword(encoderService.generateHash());
        usersRepository.save(userOld);
        log.info("Pass of user with ID: {} has been changed", userOld.getId());
        return true;
    }

    public List<UserDto> getAll() {
        return usersRepository.findAll().stream()
                .map(userMapper::userToDto)
                .collect(Collectors.toList());
    }


}
