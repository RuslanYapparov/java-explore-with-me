package ru.practicum.explore_with_me.main_service.model.db_entities;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;

import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QLikeEntityTest {
    private final QLikeEntity qLikeEntity = QLikeEntity.likeEntity;

    @Test
    public void createListOfConditionsTest() {
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(qLikeEntity.id.in(new ArrayList<>(List.of(BigInteger.ONE, BigInteger.TWO))));
        conditions.add(qLikeEntity.user.id.gt(0L));
        conditions.add(qLikeEntity.event.state.eq(EventState.PUBLISHED.name()));
        conditions.add(qLikeEntity.isLike.eq(true));
        conditions.add(qLikeEntity.clickedOn.before(LocalDateTime.now()));

        assertEquals(5, conditions.size());
    }

}