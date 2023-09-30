package secretSanta.service;

import com.pengrad.telegrambot.model.Update;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import secretSanta.security.EncoderService;
import secretSanta.security.Roles;
import secretSanta.entity.Chat;
import secretSanta.entity.User;
import secretSanta.entityDto.NewPasswordDto;
import secretSanta.entityDto.RegisterReqDto;
import secretSanta.exception.ChatNotFoundException;
import secretSanta.exception.UserAlreadyExists;
import secretSanta.exception.UserNotFoundException;
import secretSanta.security.UserDetailsCustom;
import secretSanta.security.UserDetailsServiceCustom;
import secretSanta.service.Telegram.UpdateMapperTB;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;


@Service
@Slf4j
public class AuthenticateService {
    private final UserDetailsServiceCustom userDetailsServiceCustom;
    private final UserDetailsManager manager;
    private final EncoderService encoderService;
    private final UserService userService;
    private final ChatService chatService;

    public AuthenticateService(
            UserDetailsServiceCustom userDetailsServiceCustom,
            @Qualifier("jdbcUserDetailsManager") UserDetailsManager manager,
            UserService userService,
            ChatService chatService,
            EncoderService encoderService) {
        this.userDetailsServiceCustom = userDetailsServiceCustom;
        this.manager = manager;
        this.userService = userService;
        this.chatService = chatService;
        this.encoderService = encoderService;
    }

    /**
     * Login boolean.
     *
     * @param userName the username
     * @param password the password
     * @return the boolean
     * @throws UserNotFoundException the user not found exception
     */
    public boolean login(String userName, String password) throws UserNotFoundException {
        if (!manager.userExists(userName)) {
            log.error("Failed authorization attempt. Cause:");
            log.warn("User with userName: {} not found", userName);
            throw new UserNotFoundException(userName);
        }
        UserDetails userDetails = manager.loadUserByUsername(userName);
        String encryptedPassword = userDetails.getPassword();
        boolean isLoggedIn = encoderService.matches(password, encryptedPassword);
        if (isLoggedIn) {
            log.info("User with userName: {} successfully logged in", userName);
        } else {
            log.warn("Failed authorization attempt.  Cause:");
            log.warn("Attempt to enter an incorrect password by userName:{}", userName);
        }
        return isLoggedIn;
    }

    /**
     * Register boolean.
     *
     * @param registerReq the register req
     * @return the boolean
     */
    public boolean register(RegisterReqDto registerReq) {
        if (manager.userExists(registerReq.getUsername())) {
            log.error("User with userName: {} already exists", registerReq.getUsername());
            throw new UserAlreadyExists(registerReq.getUsername());
        }
        User user =
                userService.addUser(registerReq, encoderService.getHash(registerReq.getPassword()));
        if (user != null
                && user.getUsername() != null
                && user.getUsername().equals(registerReq.getUsername())
                && user.getAuthorities() != null) {
            log.info("New user with username: {} has been registered", registerReq.getUsername());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Change password boolean.
     *
     * @param body the body
     * @return the boolean
     */
    public boolean changePassword(NewPasswordDto body) {
        manager.changePassword(
                body.getCurrentPassword(),
                encoderService.getHash(body.getNewPassword()));
        UserDetails userDetails =
                manager.loadUserByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        String encryptedPassword = userDetails.getPassword();
        boolean isChangedPassword = encoderService.matches(body.getNewPassword(), encryptedPassword);
        if (isChangedPassword) {
            log.info("User with userName: {} has been successfully changed password", userDetails.getUsername());
        } else {
            log.warn("Failed change password attempt. Cause: ");
            log.warn("Attempt to enter an incorrect password by userName:{}", userDetails.getUsername());
        }
        return isChangedPassword;
    }

    public void authenticate(String username) {
        saveContext(userDetailsServiceCustom.loadUserByUsername(username));
    }

    @Transactional
    public Authentication authenticate(Update update) {
        User user = null;
        Long idChat = UpdateMapperTB.getIdChat(update);
        String username = UpdateMapperTB.getUsername(update);
        String firstName = UpdateMapperTB.getFirstName(update);
        String lastName = UpdateMapperTB.getLastName(update);
        String displayName = UpdateMapperTB.getDisplayName(update);
        try {
            user = userService.getUserByIdTelegramChat(idChat);
        } catch (UserNotFoundException ignored) {
        }
        int index = 1;
        while (userService.existUsername(username)) {
            username = username + index;
            index++;
        }
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setDisplayName(displayName);
            user = userService.addUser(user);
        }
        Chat chat = user.getChatTelegram();
        if (chat == null) {
            try {
                chat = chatService.findChatWithUnfinishedRequest(idChat);
                chat = chatService.linkChatUser(chat, user);
            } catch (ChatNotFoundException ignored) {
            }
            if (chat == null) {
                chat = new Chat();
                chat.setId(idChat);
            }
        }
        if (chat.getLastActivity() == null || chat.getLastActivity().plusMinutes(5).isAfter(LocalDateTime.now())) {
            chat.setUser(user);
            chat.setFirstNameUser(firstName);
            chat.setLastNameUser(lastName);
            chat.setUserNameTelegram(username);
            chat.setLastActivity(LocalDateTime.now());
            chat = chatService.updateChat(chat);
        }
        if (user.getChatTelegram() == null) {
            user.setChatTelegram(chat);
        }
        UserDetails userDetails = userDetailsServiceCustom.getUserDetails(user);
        return saveContext(userDetails);
    }

    private Authentication saveContext(UserDetails userDetails) {
        Collection<GrantedAuthority> authorities = new HashSet<>(userDetails.getAuthorities());
        authorities.add(new SimpleGrantedAuthority(Roles.CLIENT.name()));

        Authentication authentication = userDetailsServiceCustom.getUsernamePasswordAuthenticationToken(
                userDetails,
                authorities);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        return authentication;
    }

    private User getCurrentUser() {
        return Optional.ofNullable(
                ((UserDetailsCustom)
                        SecurityContextHolder.getContext().getAuthentication()
                                .getPrincipal())
                        .getUser()
        ).orElseThrow(() -> new UserNotFoundException("User is absent"));
    }

}
