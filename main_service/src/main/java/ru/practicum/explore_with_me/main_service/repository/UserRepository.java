package ru.practicum.explore_with_me.main_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.practicum.explore_with_me.main_service.model.db_entities.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Page<UserEntity> findAllByIdIn(long[] ids, Pageable page);

}