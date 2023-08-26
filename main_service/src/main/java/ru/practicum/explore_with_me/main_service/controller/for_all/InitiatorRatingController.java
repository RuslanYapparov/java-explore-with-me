package ru.practicum.explore_with_me.main_service.controller.for_all;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.main_service.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/initiators/rating")
@Slf4j
@RequiredArgsConstructor
public class InitiatorRatingController {
    private final UserService userService;

    @GetMapping
    public List<UserRestView> getInitiatorsSortedByRating(
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "asc", defaultValue = "false") boolean asc) {
        log.debug("New request for page of initiators {} sorted by rating was received. " +
                "Paging parameters: from ={}, size={}", asc ? "ascending" : "descending", from, size);
        return userService.getInitiatorsSortedByRating(from, size, asc);
    }


}