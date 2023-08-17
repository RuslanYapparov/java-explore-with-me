package ru.practicum.explore_with_me.stats_service.dto_submodule.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EwmConstants {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final LocalDateTime DEFAULT_DATE_TIME = LocalDateTime.of(2023, 8, 1, 0, 0, 1);

}
