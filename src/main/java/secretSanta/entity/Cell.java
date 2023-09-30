package secretSanta.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "cell")
@ToString(exclude = {"userAliases", "owner"})
@Table(indexes = @Index(columnList = "token_db"))
public class Cell {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @OneToOne()
    @JoinColumn(name = "user_alias_id")
    @NotNull
    private UserAlias owner;
    @Column(name = "create_date")
    private LocalDate createDate;
    @Column(name = "mailing_date")
    private LocalDate mailingDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @OneToMany(mappedBy = "cell")
    private List<UserAlias> userAliases;
    @Column(name = "token")
    private String tokenHash;
    @Column(name = "token_db")
    private String tokenDB;
    @Column(name = "lock")
    private Boolean lock;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return Objects.equals(id, cell.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
