package ru.practicum.explore_with_me.main_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long>, QuerydslPredicateExecutor<EventEntity> {

    Page<EventEntity> findAllByInitiatorId(long initiatorId, Pageable page);

}