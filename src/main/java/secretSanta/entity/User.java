package secretSanta.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "users")
@Table(indexes = @Index(columnList = "username"))
@ToString(exclude = {"password", "authorities", "aliases"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "display_name")
    private String displayName;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Chat chatTelegram;
    @OneToMany(mappedBy = "user")
    private List<Authority> authorities;
    @Column(name = "reg_date")
    @PastOrPresent
    @NotNull
    private LocalDate regDate;
    @Column(name = "password", nullable = false)
    @NotNull
    private String password;
    @Column(name = "username")
    @NotNull
    private String username;
    @Column(name = "enabled")
    @NotNull
    private Boolean enabled;
    @OneToMany(mappedBy = "user")
    private List<UserAlias> aliases;
}
