package ru.filatov.chattgbot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.filatov.chattgbot.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserId(Long userId);
    List<Subscription> findByUserIdAndEndDateAfter(Long userId, LocalDateTime endDate);

    Optional<Subscription> findFirstByUserIdAndEndDateAfterAndModelNameOrderByEndDateDesc(Long userId, LocalDateTime endDate, String modelName);

    @Query("SELECT COUNT(cu) FROM ChatUsage cu WHERE cu.user.id = :userId AND cu.chatVersion = :chatVersion AND cu.lastActivity >= :startDate")
    int countRequestsMadeInCurrentPeriod(@Param("userId") Long userId, @Param("chatVersion") String chatVersion, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(cu.tokensSent) FROM ChatUsage cu WHERE cu.user.id = :userId AND cu.chatVersion = :chatVersion AND cu.lastActivity >= :startDate")
    int countTokensSentInCurrentPeriod(@Param("userId") Long userId, @Param("chatVersion") String chatVersion, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(cu.tokensReceived) FROM ChatUsage cu WHERE cu.user.id = :userId AND cu.chatVersion = :chatVersion AND cu.lastActivity >= :startDate")
    int countTokensReceivedInCurrentPeriod(@Param("userId") Long userId, @Param("chatVersion") String chatVersion, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(cu) FROM ChatUsage cu WHERE cu.user.id = :userId AND cu.lastActivity >= :startDate")
    int countRequestsMadeInCurrentPeriod(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
}
