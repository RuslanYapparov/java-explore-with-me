package ru.practicum.explore_with_me.main_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.practicum.explore_with_me.main_service.model.db_entities.RequestEntity;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, BigInteger> {

    List<RequestEntity> findAllByRequesterId(long requesterId);

    List<RequestEntity> findAllByEventId(long eventId);

    List<RequestEntity> findAllByIdIn(BigInteger[] requestIds);

}