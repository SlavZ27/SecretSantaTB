package secretSanta.security;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import secretSanta.entity.User;
import secretSanta.service.UserService;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceCustom implements UserDetailsService {
    private final UserService userService;

    public UserDetailsServiceCustom(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return getUserDetails(userService.getUserByUsername(username));
    }

    @Cacheable
    public UserDetails loadUserByIdChat(Long idChat) {
        return getUserDetails(userService.getUserByIdTelegramChat(idChat));
    }

    public UserDetails getUserDetails(User user) {
        UserDetailsCustom userDetailsCustom = new UserDetailsCustom();
        userDetailsCustom.setUsername(user.getUsername());
        userDetailsCustom.setPassword(user.getPassword());
        userDetailsCustom.setEnabled(user.getEnabled());
        userDetailsCustom.setUser(user);
        userDetailsCustom.setChat(user.getChatTelegram());
        userDetailsCustom.setAuthorities(user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority().getRole()))
                .collect(Collectors.toSet()));
        return userDetailsCustom;
    }

    public UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                userDetails.getAuthorities());
    }

    public UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(
            UserDetails userDetails, Collection<GrantedAuthority> authorities) {
        authorities.addAll(userDetails.getAuthorities());
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                userDetails.getPassword(),
                authorities);
    }
}
