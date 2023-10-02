package secretSanta.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import secretSanta.entity.Cell;
import secretSanta.entity.User;
import secretSanta.entity.UserAlias;

import java.util.List;
import java.util.Optional;

@Repository
public interface CellRepository extends JpaRepository<Cell, Long> {

    @Query(value = """ 
            select c from cell c
            left join c.userAliases ua
            where :userAliasId=ua.id and ua.enable
            """)
    Optional<Cell> findByUserAlias(@Param(value = "userAliasId") Long userAliasId);

    @Query(value = """ 
            select c from cell c
            where c.tokenDB=:tokenDB
            """)
    Optional<Cell> findByTokenDB(@Param(value = "tokenDB") String tokenDB);

    @Query(value = """ 
            select count(c) from cell c
            where c.tokenDB=:tokenDB
            """)
    int countByTokenDB(@Param(value = "tokenDB") String tokenDB);

    @Query(value = """ 
            select count(c) from cell c
            left join user_alias ua on ua in elements(c.userAliases)
            where c.id=:cellId and ua.user.id=:userId and ua.enable
            """)
    int count(@Param(value = "userId") Long userId, @Param(value = "cellId") Long cellId);

    @Query(value = """ 
            select count(ua) from cell c
            left join user_alias ua on ua in elements(c.userAliases)
            where c.id=:cellId and ua.enable
            """)
    int countUserAliases(@Param(value = "cellId") Long cellId);

    @Query(value = """ 
            select ua from cell c
            left join user_alias ua on ua in elements(c.userAliases)
            where c.id=:cellId and ua.enable
            """)
    List<UserAlias> getUserAliases(@Param(value = "cellId") Long cellId);

    @Query(value = """ 
            select c from cell c
            where c.lock=false and c.mailingDate<=current_date()
            """)
    List<Cell> findUnlockCellForMailing();
}
