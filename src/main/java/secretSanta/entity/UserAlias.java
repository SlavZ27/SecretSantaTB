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
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "user_alias")
@Table(indexes = @Index(columnList = "cell_id, user_id"),
        uniqueConstraints = {@UniqueConstraint(
                name = "UniqueCellAndUser",
                columnNames = {"cell_id", "user_id"})})
@ToString(exclude = "recipient")
public class UserAlias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "display_name")
    private String displayName;
    @Column(name = "dreams")
    private String dreams;
    @OneToOne
    @JoinColumn(name = "recipient")
    private UserAlias recipient;
    @ManyToOne
    private Cell cell;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "enable", nullable = false)
    private Boolean enable = true;
    private boolean requestDreamRecipient = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAlias userAlias = (UserAlias) o;
        return Objects.equals(id, userAlias.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
