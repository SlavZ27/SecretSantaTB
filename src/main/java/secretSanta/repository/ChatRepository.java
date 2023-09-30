package secretSanta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import secretSanta.entity.Chat;
import secretSanta.entity.User;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query(value = """ 
            select telegram_chat.* from telegram_chat
            where telegram_chat.id=:idChat
            """, nativeQuery = true)
    Optional<Chat> getChatById(@Param(value = "idChat") Long idChat);


    @Query(value = """
            select tc from telegram_chat tc
                left join fetch tc.ongoingRequestTB
            where tc.id=:idChat
            """)
    Optional<Chat> getChatByIdWithUnfinishedRequest(@Param(value = "idChat") Long idChat);

}
