package secretSanta.entityDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ChatDto {
    @JsonProperty("id")
    Long id;
    @JsonProperty("userNameTelegram")
    String userNameTelegram;
    @JsonProperty("firstNameUser")
    String firstNameUser;
    @JsonProperty("lastNameUser")
    String lastNameUser;
    @JsonProperty("lastActivity")
    LocalDateTime lastActivity;

}
