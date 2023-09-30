package secretSanta.entityDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;


public class LoginReqDto {
    @JsonProperty("password")
    @NotNull
    private String password;

    @JsonProperty("username")
    @Email
    @NotNull
    private String username;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
