package secretSanta.repository;

import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import secretSanta.entity.Chat;
import secretSanta.entity.OngoingRequestTB;

import java.util.Optional;

/**
 * This class was created to use the database to create methods used in the class UnfinishedRequestService
 */
@Repository
public interface OngoingRequestTBRepository extends JpaRepository<OngoingRequestTB, Long> {

    @Query(value = "select ort from ongoing_request_telegram ort where ort.chat.id=:idChat")
    Optional<OngoingRequestTB> findByIdChat(@Param(value = "idChat") Long idChat);

    @NotNull
    @Query(value = "select ort from ongoing_request_telegram ort where ort.id=:id")
    Optional<OngoingRequestTB> findById(@Param(value = "id") @NotNull Long id);

    @Query(value = "delete from ongoing_request_telegram ort where ort.chat.id=:idChat")
    @Modifying
    @Transactional
    void delete(@Param(value = "idChat") Long idChat);
}
