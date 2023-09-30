package secretSanta.service.Telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import secretSanta.command.Command;
import secretSanta.security.Roles;
import secretSanta.entity.Chat;
import secretSanta.security.UserDetailsCustom;

import java.util.*;


@Service
public class CommandServiceTB {
    private final Logger logger = LoggerFactory.getLogger(CommandServiceTB.class);

    public boolean approveLaunchCommand(Command command) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Set<GrantedAuthority> authorities = new HashSet<>(authentication.getAuthorities());
            return checkCoincidence(authorities, command.getGrantedAuthorities());
        }
        return command.getRoles().contains(Roles.CLIENT);
    }

    private boolean checkCoincidence(Set<GrantedAuthority> authorities1, Set<GrantedAuthority> authorities2) {
        for (GrantedAuthority authority : authorities1) {
            if (authorities2.contains(authority)) {
                return true;
            }
        }
        return false;
    }

    public TreeSet<Command> getCommands(int indexMenu) {
        return Command.getOnlyShowCommandIndexMenu(indexMenu);
    }

    public TreeSet<Command> getFunctionCommand() {
        return Command.getFunctionCommandSort();
    }

    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private Chat getCurrentAuthenticationChat() {
        Authentication authentication = getCurrentAuthentication();
        if (!(authentication instanceof UserDetailsCustom)) {
            return null;
        }
        return ((UserDetailsCustom) authentication).getChat();
    }

}
