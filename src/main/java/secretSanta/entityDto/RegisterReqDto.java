package secretSanta.entityDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

@Value
public class RegisterReqDto {
    @JsonProperty("username")
    @NotNull
    String username;

    @JsonProperty("password")
    @NotNull
    String password;

    @JsonProperty("firstName")
    String firstName;

    @JsonProperty("lastName")
    String lastName;

    @JsonProperty("phone")
    String phone;


}
