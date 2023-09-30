package secretSanta.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ChatNotFoundException extends RuntimeException{
    public ChatNotFoundException(String message) {
        super("Chat with id = " + message + " not found");
    }
}
