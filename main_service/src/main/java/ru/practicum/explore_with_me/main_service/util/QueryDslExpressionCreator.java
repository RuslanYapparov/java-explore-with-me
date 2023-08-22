package ru.practicum.explore_with_me.main_service.util;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.experimental.UtilityClass;

import ru.practicum.explore_with_me.main_service.model.db_entities.event.QEventEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder.JpaAdminGetAllQueryParamsHolder;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder.JpaPublicGetAllQueryParamsHolder;

import java.time.LocalDateTime;
import java.util.*;

@UtilityClass
public class QueryDslExpressionCreator {

    public static BooleanExpression prepareConditionsForQuery(JpaPublicGetAllQueryParamsHolder paramsHolder) {
        QEventEntity qEventEntity = QEventEntity.eventEntity;
        List<BooleanExpression> conditions = new ArrayList<>();
        conditions.add(qEventEntity.state.like(EventState.PUBLISHED.name()));
        String text = paramsHolder.getText();
        if (text != null && !text.isBlank()) {
            conditions.add(qEventEntity.annotation.containsIgnoreCase(text)
                    .or(qEventEntity.description.containsIgnoreCase(text)));
        }
        long[] categories = paramsHolder.getCategories();
        if (categories != null) {
            conditions.add(qEventEntity.category.id.in(getCollectionFromLongArray(categories)));
        }
        Boolean paid = paramsHolder.getPaid();
        if (paid != null) {
            conditions.add(qEventEntity.paid.eq(paid));
        }
        LocalDateTime rangeStart = paramsHolder.getRangeStart();
        if (rangeStart != null) {
            conditions.add(qEventEntity.eventDate.after(rangeStart));
        }
        LocalDateTime rangeEnd = paramsHolder.getRangeEnd();
        if (rangeEnd != null) {
            conditions.add(qEventEntity.eventDate.before(rangeEnd));
        }
        if (paramsHolder.isOnlyAvailable()) {
            conditions.add(qEventEntity.confirmedRequests.lt(qEventEntity.participantLimit));
        }
        return makeFinalCondition(conditions);
    }

    public static BooleanExpression prepareConditionsForQuery(JpaAdminGetAllQueryParamsHolder paramsHolder) {
        QEventEntity qEventEntity = QEventEntity.eventEntity;
        List<BooleanExpression> conditions = new ArrayList<>();
        long[] users = paramsHolder.getUsers();
        if (users != null) {
            conditions.add(qEventEntity.initiator.id.in(getCollectionFromLongArray(users)));
        }
        long[] categories = paramsHolder.getCategories();
        if (categories != null) {
            conditions.add((qEventEntity.category.id.in(getCollectionFromLongArray(categories))));
        }
        String[] states = paramsHolder.getStates();
        if (states != null) {
            conditions.add(qEventEntity.state.in(new ArrayList<>(Arrays.asList(states))));
        }
        LocalDateTime rangeStart = paramsHolder.getRangeStart();
        if (rangeStart != null) {
            conditions.add(qEventEntity.eventDate.after(rangeStart));
        }
        LocalDateTime rangeEnd = paramsHolder.getRangeEnd();
        if (rangeEnd != null) {
            conditions.add(qEventEntity.eventDate.before(rangeEnd));
        }
        return makeFinalCondition(conditions);
    }

    private BooleanExpression makeFinalCondition(List<BooleanExpression> conditions) {
        return conditions.stream()
                .reduce(BooleanExpression::and)
                .orElseThrow(() -> new UnsupportedOperationException("Fatal problem with preparing filter " +
                        "for requested events"));
    }

    private Collection<Long> getCollectionFromLongArray(long[] array) {
        Collection<Long> collection = new ArrayList<>();
        Arrays.stream(array).forEach(collection::add);
        return collection;
    }

}