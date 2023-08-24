package ru.practicum.explore_with_me.main_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.main_service.model.db_entities.LikeEntity;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, BigInteger> {

    Optional<LikeEntity> findByUserIdAndEventId(long userId, long eventId);

    List<LikeEntity> findAllByUserId(long userId);

    List<LikeEntity> findAllByEventId(long eventId);

    List<LikeEntity> findAllByEventIdAndEventEventDateBefore(long id, LocalDateTime now);

}