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
public interface UserAliasRepository extends JpaRepository<UserAlias, Long> {

    @Query(value = """ 
            select ua from user_alias ua
            left join user u on ua.user in elements(u.aliases)
            left join ua.recipient
            where ua.cell.id=:cellId and ua.enable
            ORDER BY ua.displayName
            """)
    List<UserAlias> findByCell(@Param(value = "cellId") Long cellId);

    @Query(value = """ 
            select ua from user_alias ua
            left join user u on ua.user in elements(u.aliases)
            where u.id=:userId and ua.enable
            ORDER BY ua.displayName
            """)
    List<UserAlias> findByUser(@Param(value = "userId") Long userId);

    @Query(value = """ 
            select ua from user_alias ua
            left join user u on ua.user in elements(u.aliases)
            where u.id=:userId and ua.cell.id=:cellId
            """)
    Optional<UserAlias> findByUserAndCellIncludeDeleted(@Param(value = "userId") Long userId, @Param(value = "cellId") Long cellId);

    @Query(value = """ 
            select ua from user_alias ua
            left join user u on ua.user in elements(u.aliases)
            where u.id=:userId and ua.cell.id=:cellId and ua.enable
            """)
    Optional<UserAlias> findByUserAndCell(@Param(value = "userId") Long userId, @Param(value = "cellId") Long cellId);

    @NotNull
    @Query(value = """ 
            select ua from user_alias ua
            left join user u on ua.user in elements(u.aliases)
            where ua.id=:userAliasId and ua.enable
            """)
    Optional<UserAlias> findById(@Param(value = "userAliasId") @NotNull Long userAliasId);

    @Query(value = """ 
            select count(ua) from user_alias ua
            where ua.user.id=:userId and ua.enable
            """)
    int countUserAliases(@Param(value = "userId") Long userId);

    @Query(value = """ 
            select ua from user_alias ua
            where ua.recipient.id=:recipientId and ua.enable
            """)
    Optional<UserAlias> findByRecipient(@Param(value = "recipientId") Long recipientId);
}

