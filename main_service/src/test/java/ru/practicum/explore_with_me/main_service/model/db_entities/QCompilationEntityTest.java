package ru.practicum.explore_with_me.main_service.model.db_entities;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;

import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QCompilationEntityTest {
    private final QCompilationEntity qCompilationEntity = QCompilationEntity.compilationEntity;

    @Test
    public void createListOfConditionsTest() {
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(qCompilationEntity.id.in(new ArrayList<>(List.of(1L, 2L))));
        conditions.add(qCompilationEntity.events.contains(new EventEntity()));
        conditions.add(qCompilationEntity.title.eq("title"));
        conditions.add(qCompilationEntity.pinned.eq(true));

        assertEquals(4, conditions.size());
    }

}