package ru.practicum.explore_with_me.main_service.mapper;

public interface ObjectMapper<E, M, C, V> {
    /*
    * E - entity class
    * M - domain class
    * C - rest-command class
    * V - rest-view class */

    M fromRestCommand(C commandObject);

    V toRestView(M domainObject);

    M fromDbEntity(E entityObject);

    E toDbEntity(M domainObject);

}