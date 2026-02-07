package com.rannett.fixplugin.quickfix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Loads QuickFIX session configuration metadata and provides value validation helpers.
 */
public final class QuickFixConfigSettings {

    private static final String RESOURCE_PATH = "/documentation/quickfix-session-config.tsv";
    private static final Pattern QUOTED_VALUE_PATTERN = Pattern.compile("\"([^\"]+)\"");
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2})(?:\\s+(.+))?$");
    private static final Pattern ENUM_TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");

    private static final Set<String> ENUM_STOP_WORDS = Set.of(
            "positive",
            "integer",
            "number",
            "string",
            "format",
            "time",
            "day",
            "comma",
            "list",
            "valid",
            "default",
            "class",
            "directory",
            "alpha-numeric",
            "open",
            "socket",
            "ip",
            "domain",
            "hostnames",
            "host",
            "value",
            "values",
            "non-negative",
            "zone",
            "any",
            "one",
            "of"
    );

    private static final Map<String, QuickFixConfigSetting> SETTINGS = loadSettings();

    private QuickFixConfigSettings() {
    }

    /**
     * Returns the settings loaded from the bundled session configuration metadata.
     *
     * @return the settings map keyed by configuration name.
     */
    public static @NotNull Map<String, QuickFixConfigSetting> getSettings() {
        return SETTINGS;
    }

    /**
     * Returns the setting metadata for the provided key.
     *
     * @param key the configuration key.
     * @return the setting metadata when available.
     */
    public static @NotNull Optional<QuickFixConfigSetting> findSetting(@NotNull String key) {
        return Optional.ofNullable(SETTINGS.get(key));
    }

    /**
     * Validates a value for the provided configuration key.
     *
     * @param key the configuration key.
     * @param value the configuration value.
     * @return a validation error message if invalid.
     */
    public static @NotNull Optional<String> validateValue(@NotNull String key, @NotNull String value) {
        Optional<QuickFixConfigSetting> setting = findSetting(key);
        if (setting.isEmpty()) {
            return Optional.empty();
        }
        if (value.isBlank()) {
            return Optional.empty();
        }
        return setting.get().validator().flatMap(validator -> validator.validate(value));
    }

    private static Map<String, QuickFixConfigSetting> loadSettings() {
        try (InputStream stream = QuickFixConfigSettings.class.getResourceAsStream(RESOURCE_PATH)) {
            if (stream == null) {
                return Map.of();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(line -> line.split("\t", -1))
                        .filter(parts -> parts.length >= 2)
                        .map(QuickFixConfigSettings::toSetting)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(
                                QuickFixConfigSetting::key,
                                setting -> setting,
                                (first, second) -> first,
                                LinkedHashMap::new
                        ));
            }
        } catch (IOException exception) {
            return Map.of();
        }
    }

    private static @Nullable QuickFixConfigSetting toSetting(String[] parts) {
        String key = parts[0].trim();
        if (key.isEmpty()) {
            return null;
        }
        String description = parts[1].trim();
        String values = parts.length > 2 ? parts[2].trim() : "";
        String defaultValue = parts.length > 3 ? parts[3].trim() : "";
        Optional<ValueValidator> validator = parseValidator(values);
        return new QuickFixConfigSetting(key, description, values, defaultValue, validator);
    }

    private static Optional<ValueValidator> parseValidator(String values) {
        if (values == null || values.isBlank()) {
            return Optional.empty();
        }
        String trimmed = values.trim();
        if ("Y N".equals(trimmed)) {
            return Optional.of(enumValidator(Set.of("Y", "N")));
        }
        if (trimmed.contains("<tag>=<value>")) {
            return Optional.of(tagValueValidator());
        }
        if (trimmed.contains("case-sensitive alpha-numeric string")) {
            return Optional.of(alphaNumericValidator());
        }
        if (trimmed.startsWith("Time zone ID")) {
            return Optional.of(timeZoneValidator());
        }
        if (trimmed.startsWith("time in the format of HH:MM:SS")) {
            return Optional.of(timeWithOptionalZoneValidator());
        }
        if (trimmed.contains("positive integer, valid open socket port")) {
            return Optional.of(portValidator());
        }
        if (containsAnyIgnoreCase(trimmed, "valid IP address", "hostname or IP address", "hostnames or IP addresses")) {
            return Optional.of(hostOrIpValidator());
        }
        if (containsAnyIgnoreCase(trimmed, "any positive integer", "positive integer", "positive Integer")) {
            return Optional.of(positiveIntegerValidator());
        }
        if (containsAnyIgnoreCase(trimmed, "positive number")) {
            return Optional.of(positiveNumberValidator());
        }
        if (containsAnyIgnoreCase(trimmed, "non-negative number", "any non-negative value")) {
            return Optional.of(nonNegativeNumberValidator());
        }
        if (containsAnyIgnoreCase(trimmed, "integer value", "Integer value", "Integer.")) {
            return Optional.of(integerValidator());
        }

        Optional<Set<String>> quotedValues = parseQuotedValues(trimmed);
        if (quotedValues.isPresent()) {
            return Optional.of(enumValidator(quotedValues.get()));
        }

        if (trimmed.startsWith("One of ")) {
            Set<String> tokens = extractEnumTokens(trimmed.substring("One of ".length()));
            if (!tokens.isEmpty()) {
                return Optional.of(enumValidator(tokens));
            }
        }

        Optional<Set<String>> enumValues = parseSimpleEnum(trimmed);
        if (enumValues.isPresent()) {
            return Optional.of(enumValidator(enumValues.get()));
        }

        return Optional.empty();
    }

    private static Optional<Set<String>> parseQuotedValues(String values) {
        Matcher matcher = QUOTED_VALUE_PATTERN.matcher(values);
        Set<String> tokens = matcher.results()
                .map(result -> result.group(1))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (tokens.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tokens);
    }

    private static Optional<Set<String>> parseSimpleEnum(String values) {
        if (values.contains(",")) {
            return Optional.empty();
        }
        String[] tokens = values.split("\\s+");
        if (tokens.length < 2) {
            return Optional.empty();
        }
        Set<String> normalized = Arrays.stream(tokens)
                .filter(token -> !token.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        boolean hasStopWord = normalized.stream()
                .map(token -> token.toLowerCase(Locale.ROOT))
                .anyMatch(ENUM_STOP_WORDS::contains);
        if (hasStopWord) {
            return Optional.empty();
        }
        boolean allSimple = normalized.stream().allMatch(token -> ENUM_TOKEN_PATTERN.matcher(token).matches());
        if (!allSimple) {
            return Optional.empty();
        }
        return Optional.of(normalized);
    }

    private static Set<String> extractEnumTokens(String valueText) {
        return Arrays.stream(valueText.split("\\s+"))
                .map(token -> token.replaceAll("[^A-Za-z0-9_]", ""))
                .filter(token -> !token.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean containsAnyIgnoreCase(String value, String... tokens) {
        String lower = value.toLowerCase(Locale.ROOT);
        return Arrays.stream(tokens)
                .map(token -> token.toLowerCase(Locale.ROOT))
                .anyMatch(lower::contains);
    }

    private static ValueValidator enumValidator(Set<String> allowed) {
        Set<String> allowedValues = Set.copyOf(allowed);
        String message = "Value must be one of: " + String.join(", ", allowedValues) + ".";
        return value -> allowedValues.contains(value)
                ? Optional.empty()
                : Optional.of(message);
    }

    private static ValueValidator positiveIntegerValidator() {
        return value -> validateInteger(value, integerValue -> integerValue > 0,
                "Value must be a positive integer.");
    }

    private static ValueValidator integerValidator() {
        return value -> validateInteger(value, integerValue -> true,
                "Value must be an integer.");
    }

    private static ValueValidator portValidator() {
        return value -> validateInteger(value, integerValue -> integerValue > 0 && integerValue <= 65535,
                "Value must be a valid TCP port (1-65535).");
    }

    private static ValueValidator positiveNumberValidator() {
        return value -> validateDecimal(value, decimal -> decimal.compareTo(BigDecimal.ZERO) > 0,
                "Value must be a positive number.");
    }

    private static ValueValidator nonNegativeNumberValidator() {
        return value -> validateDecimal(value, decimal -> decimal.compareTo(BigDecimal.ZERO) >= 0,
                "Value must be a non-negative number.");
    }

    private static Optional<String> validateInteger(String value, java.util.function.IntPredicate predicate, String message) {
        try {
            int integerValue = Integer.parseInt(value.trim());
            if (predicate.test(integerValue)) {
                return Optional.empty();
            }
        } catch (NumberFormatException exception) {
            return Optional.of(message);
        }
        return Optional.of(message);
    }

    private static Optional<String> validateDecimal(
            String value,
            java.util.function.Predicate<BigDecimal> predicate,
            String message
    ) {
        try {
            BigDecimal decimal = new BigDecimal(value.trim());
            if (predicate.test(decimal)) {
                return Optional.empty();
            }
        } catch (NumberFormatException exception) {
            return Optional.of(message);
        }
        return Optional.of(message);
    }

    private static ValueValidator timeZoneValidator() {
        return value -> {
            try {
                ZoneId.of(value.trim());
                return Optional.empty();
            } catch (DateTimeException exception) {
                return Optional.of("Value must be a valid time zone ID.");
            }
        };
    }

    private static ValueValidator timeWithOptionalZoneValidator() {
        return value -> {
            Matcher matcher = TIME_PATTERN.matcher(value.trim());
            if (!matcher.matches()) {
                return Optional.of("Value must use HH:MM:SS with an optional timezone.");
            }
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));
            int second = Integer.parseInt(matcher.group(3));
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
                return Optional.of("Value must use a valid time in HH:MM:SS format.");
            }
            String zone = matcher.group(4);
            if (zone != null) {
                try {
                    ZoneId.of(zone.trim());
                } catch (DateTimeException exception) {
                    return Optional.of("Value must include a valid timezone ID.");
                }
            }
            return Optional.empty();
        };
    }

    private static ValueValidator hostOrIpValidator() {
        return value -> {
            String trimmed = value.trim();
            if (isValidIpv4(trimmed)) {
                return Optional.empty();
            }
            if (trimmed.matches("^[A-Za-z0-9.-]+$") && trimmed.chars().anyMatch(Character::isLetter)) {
                return Optional.empty();
            }
            return Optional.of("Value must be a valid IP address or hostname.");
        };
    }

    private static ValueValidator tagValueValidator() {
        return value -> {
            String trimmed = value.trim();
            int index = trimmed.indexOf('=');
            if (index <= 0 || index == trimmed.length() - 1) {
                return Optional.of("Value must be in the format <tag>=<value>.");
            }
            String tagValue = trimmed.substring(0, index);
            try {
                int tag = Integer.parseInt(tagValue);
                if (tag > 0) {
                    return Optional.empty();
                }
            } catch (NumberFormatException exception) {
                return Optional.of("Value must start with a positive integer tag.");
            }
            return Optional.of("Value must start with a positive integer tag.");
        };
    }

    private static ValueValidator alphaNumericValidator() {
        return value -> value.matches("^[A-Za-z0-9]+$")
                ? Optional.empty()
                : Optional.of("Value must be an alphanumeric string.");
    }

    private static boolean isValidIpv4(String value) {
        String[] parts = value.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        return Arrays.stream(parts)
                .map(QuickFixConfigSettings::parseOctet)
                .allMatch(Optional::isPresent);
    }

    private static Optional<Integer> parseOctet(String value) {
        try {
            int octet = Integer.parseInt(value);
            if (octet < 0 || octet > 255) {
                return Optional.empty();
            }
            return Optional.of(octet);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface ValueValidator {
        Optional<String> validate(String value);
    }

    /**
     * Represents a QuickFIX session configuration option.
     *
     * @param key the configuration key.
     * @param description the configuration description.
     * @param values the accepted values, when provided.
     * @param defaultValue the default value, when provided.
     * @param validator optional validation rules for the value.
     */
    public record QuickFixConfigSetting(
            @NotNull String key,
            @NotNull String description,
            @NotNull String values,
            @NotNull String defaultValue,
            @NotNull Optional<ValueValidator> validator
    ) {
    }
}
