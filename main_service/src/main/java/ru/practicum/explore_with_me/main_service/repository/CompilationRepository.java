package ru.practicum.explore_with_me.main_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.main_service.model.db_entities.CompilationEntity;

@Repository
public interface CompilationRepository extends JpaRepository<CompilationEntity, Long> {

    Page<CompilationEntity> findAllByPinned(boolean pinned, Pageable page);

}