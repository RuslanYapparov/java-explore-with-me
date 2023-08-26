package ru.practicum.explore_with_me.main_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore_with_me.main_service.model.db_entities.RequestEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.Request;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.RequestStatus;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.ModeratedRequestsRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestView;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    Request fromRestCommand(RequestRestCommand requestRestCommand);

    RequestRestView toRestView(Request request);

    @Mapping(target = "requester", ignore = true)
    @Mapping(target = "event", ignore = true)
    RequestEntity toDbEntity(Request request);

    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "event", source = "event.id")
    Request fromDbEntity(RequestEntity requestEntity);

    default ModeratedRequestsRestView mapModeratedRequestsRestViewFromListOfRequests(List<RequestRestView> requests) {
        ModeratedRequestsRestView moderatedRequests = ModeratedRequestsRestView.builder()
                .confirmed(new TreeSet<>(Comparator.comparing(RequestRestView::getId)))
                .rejected(new TreeSet<>(Comparator.comparing(RequestRestView::getId)))
                .build();
        requests.stream()
                .filter(request -> !RequestStatus.PENDING.name().equals(request.getStatus()) &&
                        !RequestStatus.CANCELED.name().equals(request.getStatus()))
                .forEach(request -> {
                    if (request.getStatus().equals(RequestStatus.REJECTED.name())) {
                        moderatedRequests.getRejected().add(request);
                    }
                    moderatedRequests.getConfirmed().add(request);
                });
        return moderatedRequests;
    }

}