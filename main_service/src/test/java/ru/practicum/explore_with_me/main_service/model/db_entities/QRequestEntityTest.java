package ru.practicum.explore_with_me.main_service.model.db_entities;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.Test;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.RequestStatus;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QRequestEntityTest {
    private final QRequestEntity qRequestEntity = QRequestEntity.requestEntity;

    @Test
    public void createListOfConditionsTest() {
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(qRequestEntity.id.in(new ArrayList<>(List.of(BigInteger.ONE, BigInteger.TWO))));
        conditions.add(qRequestEntity.requester.id.gt(0L));
        conditions.add(qRequestEntity.event.state.eq(EventState.PUBLISHED.name()));
        conditions.add(qRequestEntity.status.eq(RequestStatus.PENDING.name()));
        conditions.add(qRequestEntity.createdOn.before(LocalDateTime.now()));

        assertEquals(5, conditions.size());
    }

}