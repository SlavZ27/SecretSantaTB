package secretSanta.entityDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.validation.annotation.Validated;

@Value
public class UserDto {
    @JsonProperty("id")
    Long id;
    @Email(message = "Email address has invalid format: ${validatedValue}")
    @JsonProperty("email")
    String email;
    @JsonProperty("firstName")
    String firstName;
    @JsonProperty("lastName")
    String lastName;
    @JsonProperty("phone")
    String phone;
    @JsonProperty("regDate")
    String regDate;
    @JsonProperty("username")
    String username;
    @JsonProperty("enabled")
    Boolean enabled;
}
