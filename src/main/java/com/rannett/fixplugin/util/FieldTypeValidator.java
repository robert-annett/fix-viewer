package com.rannett.fixplugin.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Validates FIX field values against their expected FIX data type.
 */
public final class FieldTypeValidator {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^-?\\d+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern COUNTRY_PATTERN = Pattern.compile("^[A-Z]{2}$");
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern MONTH_YEAR_PATTERN = Pattern.compile(
            "^(\\d{4}(0[1-9]|1[0-2]))((0[1-9]|[12][0-9]|3[01])|(w[1-5]))?$");
    private static final Pattern UTC_TIMESTAMP_PATTERN =
            Pattern.compile("^\\d{8}-\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?[Zz]?$");
    private static final Pattern UTC_TIME_ONLY_PATTERN =
            Pattern.compile("^\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?$");
    private static final Pattern UTC_DATE_ONLY_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern TZ_TIME_ONLY_PATTERN =
            Pattern.compile("^\\d{2}:\\d{2}(:\\d{2})?([Zz]|([+-]\\d{2}:\\d{2}))?$");
    private static final Pattern TZ_TIMESTAMP_PATTERN =
            Pattern.compile("^\\d{8}-\\d{2}:\\d{2}(:\\d{2})?([Zz]|([+-]\\d{2}:\\d{2}))?$");
    private static final Pattern TENOR_PATTERN = Pattern.compile("^[DWMY][1-9]\\d*$");

    private static final Map<String, Predicate<String>> TYPE_VALIDATORS = buildValidators();

    private FieldTypeValidator() {
    }

    /**
     * Determines whether the provided value is valid for the specified FIX type.
     *
     * @param type the FIX field type name (case-insensitive)
     * @param value the value to validate
     * @return true when the value is valid or the type is unknown
     */
    public static boolean isValueValidForType(String type, String value) {
        if (type == null || value == null) {
            return true;
        }

        String normalizedType = type.toUpperCase(Locale.ROOT);
        Predicate<String> validator = TYPE_VALIDATORS.get(normalizedType);
        if (validator == null) {
            return true;
        }
        return validator.test(value);
    }

    private static Map<String, Predicate<String>> buildValidators() {
        Map<String, Predicate<String>> validators = new HashMap<>();
        registerValidator(validators, FieldTypeValidator::isInteger, "INT", "LENGTH", "SEQNUM");
        registerValidator(validators, value -> value.length() == 1, "CHAR");
        registerValidator(validators, FieldTypeValidator::isDecimal, "PRICE", "FLOAT", "QTY", "AMT", "PERCENTAGE");
        registerValidator(validators, FieldTypeValidator::isBoolean, "BOOLEAN");
        registerValidator(validators, FieldTypeValidator::isDayOfMonth, "DAYOFMONTH");
        registerValidator(validators, value -> matches(COUNTRY_PATTERN, value), "COUNTRY");
        registerValidator(validators, value -> matches(CURRENCY_PATTERN, value), "CURRENCY");
        registerValidator(validators, value -> matches(MONTH_YEAR_PATTERN, value), "MONTHYEAR");
        registerValidator(validators, value -> matches(UTC_TIMESTAMP_PATTERN, value), "UTCTIMESTAMP");
        registerValidator(validators, value -> matches(UTC_TIME_ONLY_PATTERN, value), "UTCTIMEONLY");
        registerValidator(validators, value -> matches(UTC_DATE_ONLY_PATTERN, value), "UTCDATEONLY", "LOCALMKTDATE");
        registerValidator(validators, value -> matches(TZ_TIME_ONLY_PATTERN, value), "TZTIMEONLY");
        registerValidator(validators, value -> matches(TZ_TIMESTAMP_PATTERN, value), "TZTIMESTAMP");
        registerValidator(validators, value -> matches(TENOR_PATTERN, value), "TENOR");
        return Collections.unmodifiableMap(validators);
    }

    private static void registerValidator(Map<String, Predicate<String>> validators,
                                          Predicate<String> validator,
                                          String... types) {
        Stream.of(types).forEach(type -> validators.put(type, validator));
    }

    private static boolean isInteger(String value) {
        return matches(INTEGER_PATTERN, value);
    }

    private static boolean isDecimal(String value) {
        return matches(DECIMAL_PATTERN, value);
    }

    private static boolean isBoolean(String value) {
        return "Y".equals(value) || "N".equals(value);
    }

    private static boolean isDayOfMonth(String value) {
        try {
            int day = Integer.parseInt(value);
            return day >= 1 && day <= 31;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static boolean matches(Pattern pattern, String value) {
        return pattern.matcher(value).matches();
    }
}
