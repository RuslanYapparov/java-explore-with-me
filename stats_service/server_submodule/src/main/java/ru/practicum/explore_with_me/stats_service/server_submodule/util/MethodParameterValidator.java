package ru.practicum.explore_with_me.stats_service.server_submodule.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.validator.routines.InetAddressValidator;
import ru.practicum.explore_with_me.stats_service.server_submodule.exception.BadRequestBodyException;
import ru.practicum.explore_with_me.stats_service.server_submodule.exception.BadRequestParameterException;

import java.time.LocalDateTime;

@UtilityClass
public class MethodParameterValidator {

    public static void validateRequestParameters(String ip, String uri) {
        if (!InetAddressValidator.getInstance().isValid(ip)) {
            throw new BadRequestBodyException("Wrong method parameter: IP not in IPv4 or IPv6 format");
        }
        if (!uri.startsWith("/events")) {
            throw new BadRequestBodyException("Wrong method parameter: URI not supported for saving hit");
        }
    }

    public static void validateRequestParameters(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestParameterException("Wrong method parameter: " +
                    "start or end of period for request cannot be null");
        }
        if (start.isAfter(end)) {
            throw new BadRequestParameterException("Wrong method parameter: " +
                    "start of period for requesting cannot be after its end");
        }
        if (start.isAfter(LocalDateTime.now())) {
            throw new BadRequestParameterException("Wrong method parameter: " +
                    "start of period for requesting cannot be after current moment");
        }
    }

}