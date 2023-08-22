package ru.practicum.explore_with_me.main_service.model.domain_pojo;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.Event;

import java.util.Set;

@Value
@Builder(toBuilder = true)
@ToString(exclude = { "eventsIds" })
public class Compilation {
    long id;
    String title;
    boolean pinned;
    Set<Long> eventsIds;
    Set<Event> events;

}