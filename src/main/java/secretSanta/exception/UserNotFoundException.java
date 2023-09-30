package secretSanta.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String username) {
        super("User with username = " + username + " not found");
    }
    public UserNotFoundException(Long idChat) {
        super("User with idChat = " + idChat + " not found");
    }
}
