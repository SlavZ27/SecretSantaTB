package secretSanta.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import secretSanta.exception.UserNotFoundException;

import java.util.Optional;

@Service
@Slf4j
public class AuthenticationManagerCustom implements AuthenticationManager {
    private final EncoderService encoderService;
    private final UserDetailsServiceCustom userDetailsServiceCustom;

    public AuthenticationManagerCustom(EncoderService encoderService, UserDetailsServiceCustom userDetailsServiceCustom) {
        this.encoderService = encoderService;
        this.userDetailsServiceCustom = userDetailsServiceCustom;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = Optional.ofNullable(authentication.getPrincipal().toString())
                .orElseThrow(() -> new BadCredentialsException("Attempt to enter an incorrect credentials"));
        UserDetails userDetails = null;
        try {
            userDetails = userDetailsServiceCustom.loadUserByUsername(userName);
        } catch (UserNotFoundException ignored) {
            throw new BadCredentialsException("Attempt to enter an incorrect credentials");
        }
        String encryptedPasswordWithoutEncryptionType = userDetails.getPassword();
        boolean isLoggedIn = encoderService.matches(
                authentication.getCredentials().toString(),
                encryptedPasswordWithoutEncryptionType);
        if (isLoggedIn) {
            log.debug("User with userName: {} successfully logged in", userName);
            return userDetailsServiceCustom.getUsernamePasswordAuthenticationToken(userDetails);
        } else {
            log.debug("Attempt to enter an incorrect password by userName:{}", userName);
            throw new BadCredentialsException("Attempt to enter an incorrect credentials");
        }
    }
}
