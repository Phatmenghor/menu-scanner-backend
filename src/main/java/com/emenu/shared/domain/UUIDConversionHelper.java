package com.emenu.shared.domain;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class UUIDConversionHelper {

    private static final String DELIMITER = ",";

    /**
     * Convert a list of UUIDs to a comma-separated string
     */
    public static String uuidListToString(List<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return null;
        }
        return uuids.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(DELIMITER));
    }

    /**
     * Convert a comma-separated string to a list of UUIDs
     */
    public static List<UUID> stringToUuidList(String uuidsString) {
        if (uuidsString == null || uuidsString.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            return Arrays.stream(uuidsString.split(DELIMITER))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.warn("Failed to parse UUID string: {}", uuidsString, e);
            return Collections.emptyList();
        }
    }

    /**
     * Convert a list of strings to a comma-separated string
     */
    public static String stringListToString(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return null;
        }
        return String.join(DELIMITER, strings);
    }

    /**
     * Convert a comma-separated string to a list of strings
     */
    public static List<String> stringToStringList(String string) {
        if (string == null || string.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(string.split(DELIMITER))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}