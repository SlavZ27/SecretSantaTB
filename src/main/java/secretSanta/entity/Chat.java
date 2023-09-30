package secretSanta.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "telegram_chat")
@ToString(exclude = "user")
public class Chat {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "user_name_telegram")
    private String userNameTelegram;
    @Column(name = "first_name_user")
    private String firstNameUser;
    @Column(name = "last_name_user")
    private String lastNameUser;
    @Column(name = "last_activity")
    @NotNull
    private LocalDateTime lastActivity = LocalDateTime.now();
    @OneToOne()
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;
    @Getter
    @OneToOne(mappedBy = "chat", cascade = CascadeType.ALL)
    private OngoingRequestTB ongoingRequestTB;

}
