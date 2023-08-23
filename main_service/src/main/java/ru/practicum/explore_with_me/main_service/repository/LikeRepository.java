package ru.practicum.explore_with_me.main_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.main_service.model.db_entities.LikeEntity;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, BigInteger> {

    List<LikeEntity> findAllByUserId(long userId);

    List<LikeEntity> findAllByEventId(long eventId);

    List<LikeEntity> findAllByEventIdAndEventEventDateAfter(long id, LocalDateTime now);

}