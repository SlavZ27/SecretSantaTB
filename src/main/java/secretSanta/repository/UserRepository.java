package secretSanta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import secretSanta.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
//    left join fetch u.authorities au
    @Query(value = """ 
            select u from users u
                left join fetch u.authorities
                left join fetch u.chatTelegram ct
                left join fetch ct.ongoingRequestTB onr
            where ct.id=:idChat
            """)
    Optional<User> getByIdChat(@Param("idChat") Long idChat);

    @Query(value = """
            select u from users u
                left join fetch u.authorities
            where username=:login
            """)
    Optional<User> getByUsername(@Param("login") String login);

    boolean existsByUsername(String username);
}