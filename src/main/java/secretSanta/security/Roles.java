package secretSanta.security;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public enum Roles {
    ADMIN("ROLE_ADMIN"),
    CLIENT("ROLE_CLIENT");
    private final String role;

    Roles(String role) {
        this.role = role;
    }

}
