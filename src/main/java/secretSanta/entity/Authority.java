package secretSanta.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import secretSanta.security.Roles;


@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "authorities")
@Table(indexes = @Index(columnList = "user_id"))
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @NotNull
    @Column(name = "authority", nullable = false)
    @Enumerated(EnumType.STRING)
    private Roles authority;
}
