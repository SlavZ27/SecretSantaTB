package secretSanta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import secretSanta.entity.Authority;

import java.util.Optional;
import java.util.Set;

/**
 * The interface Authority repository.
 */
public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    @Query(value = "select * from authorities where username=:username and authority=:authority"
            , nativeQuery = true)
    Optional<Authority> findByUsernameAndAuthority(
            @Param(value = "username") String username,
            @Param(value = "authority") String authority);

    @Query(value = "select * from authorities where username=:username"
            , nativeQuery = true)
    Set<Authority> getAllByUsername(@Param(value = "username") String username);

}
