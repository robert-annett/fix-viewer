package com.rannett.fixplugin.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FieldTypeValidatorTest {

    @Test
    public void testValidInt() {
        assertTrue(FieldTypeValidator.isValueValidForType("INT", "123"));
        assertTrue(FieldTypeValidator.isValueValidForType("INT", "-42"));
    }

    @Test
    public void testInvalidInt() {
        assertFalse(FieldTypeValidator.isValueValidForType("INT", "12A"));
    }

    @Test
    public void testValidChar() {
        assertTrue(FieldTypeValidator.isValueValidForType("CHAR", "A"));
    }

    @Test
    public void testInvalidChar() {
        assertFalse(FieldTypeValidator.isValueValidForType("CHAR", "AB"));
    }

    @Test
    public void testValidDayOfMonth() {
        assertTrue(FieldTypeValidator.isValueValidForType("DAYOFMONTH", "15"));
    }

    @Test
    public void testInvalidDayOfMonth() {
        assertFalse(FieldTypeValidator.isValueValidForType("DAYOFMONTH", "32"));
    }

    @Test
    public void testValidCountry() {
        assertTrue(FieldTypeValidator.isValueValidForType("COUNTRY", "US"));
    }

    @Test
    public void testInvalidCountry() {
        assertFalse(FieldTypeValidator.isValueValidForType("COUNTRY", "USA"));
        assertFalse(FieldTypeValidator.isValueValidForType("COUNTRY", "u1"));
    }

    @Test
    public void testValidCurrency() {
        assertTrue(FieldTypeValidator.isValueValidForType("CURRENCY", "USD"));
    }

    @Test
    public void testInvalidCurrency() {
        assertFalse(FieldTypeValidator.isValueValidForType("CURRENCY", "US"));
        assertFalse(FieldTypeValidator.isValueValidForType("CURRENCY", "1234"));
    }

    @Test
    public void testValidBoolean() {
        assertTrue(FieldTypeValidator.isValueValidForType("BOOLEAN", "Y"));
        assertTrue(FieldTypeValidator.isValueValidForType("BOOLEAN", "N"));
    }

    @Test
    public void testInvalidBoolean() {
        assertFalse(FieldTypeValidator.isValueValidForType("BOOLEAN", "X"));
    }

    @Test
    public void testValidMonthYear() {
        assertTrue(FieldTypeValidator.isValueValidForType("MonthYear", "202405"));
        assertTrue(FieldTypeValidator.isValueValidForType("MonthYear", "20240515"));
        assertTrue(FieldTypeValidator.isValueValidForType("MonthYear", "202405w3"));
    }

    @Test
    public void testInvalidMonthYear() {
        assertFalse(FieldTypeValidator.isValueValidForType("MonthYear", "202413")); // Invalid month
        assertFalse(FieldTypeValidator.isValueValidForType("MonthYear", "20240532")); // Invalid day
        assertFalse(FieldTypeValidator.isValueValidForType("MonthYear", "202405w6")); // Invalid week code
    }

    @Test
    public void testValidUTCTimestamp() {
        assertTrue(FieldTypeValidator.isValueValidForType("UTCTimestamp", "20240531-15:20:30"));
        assertTrue(FieldTypeValidator.isValueValidForType("UTCTimestamp", "20240531-15:20:30.123"));
        assertTrue(FieldTypeValidator.isValueValidForType("UTCTimestamp", "20241011-12:45:18.938000000Z"));
    }

    @Test
    public void testInvalidUTCTimestamp() {
        assertFalse(FieldTypeValidator.isValueValidForType("UTCTimestamp", "20240531 15:20:30"));
    }

    @Test
    public void testValidUTCTimeOnly() {
        assertTrue(FieldTypeValidator.isValueValidForType("UTCTimeOnly", "15:20:30"));
        assertTrue(FieldTypeValidator.isValueValidForType("UTCTimeOnly", "15:20:30.123"));
    }

    @Test
    public void testInvalidUTCTimeOnly() {
        assertFalse(FieldTypeValidator.isValueValidForType("UTCTimeOnly", "15:20"));
    }

    @Test
    public void testValidUTCDateOnly() {
        assertTrue(FieldTypeValidator.isValueValidForType("UTCDateOnly", "20240531"));
    }

    @Test
    public void testInvalidUTCDateOnly() {
        assertFalse(FieldTypeValidator.isValueValidForType("UTCDateOnly", "2024-05-31"));
    }

    @Test
    public void testValidLocalMktDate() {
        assertTrue(FieldTypeValidator.isValueValidForType("LocalMktDate", "20240531"));
    }

    @Test
    public void testInvalidLocalMktDate() {
        assertFalse(FieldTypeValidator.isValueValidForType("LocalMktDate", "31-05-2024"));
    }

    @Test
    public void testValidTZTimeOnly() {
        assertTrue(FieldTypeValidator.isValueValidForType("TZTimeOnly", "15:20Z"));
        assertTrue(FieldTypeValidator.isValueValidForType("TZTimeOnly", "15:20:30+02:00"));
        assertTrue(FieldTypeValidator.isValueValidForType("TZTimeOnly", "15:20"));
        assertTrue(FieldTypeValidator.isValueValidForType("TZTimeOnly", "15:20:10"));
    }

    @Test
    public void testInvalidTZTimeOnly() {
        assertFalse(FieldTypeValidator.isValueValidForType("TZTimeOnly", "20240531-15:20Z"));
    }

    @Test
    public void testValidTZTimestamp() {
        assertTrue(FieldTypeValidator.isValueValidForType("TZTimestamp", "20240531-15:20Z"));
        assertTrue(FieldTypeValidator.isValueValidForType("TZTimestamp", "20240531-15:20:30+02:00"));
    }

    @Test
    public void testInvalidTZTimestamp() {
        assertFalse(FieldTypeValidator.isValueValidForType("TZTimestamp", "2024-05-31 15:20Z"));
    }

    @Test
    public void testValidTenor() {
        assertTrue(FieldTypeValidator.isValueValidForType("Tenor", "D5"));
        assertTrue(FieldTypeValidator.isValueValidForType("Tenor", "M12"));
        assertTrue(FieldTypeValidator.isValueValidForType("Tenor", "W3"));
        assertTrue(FieldTypeValidator.isValueValidForType("Tenor", "Y1"));
    }

    @Test
    public void testInvalidTenor() {
        assertFalse(FieldTypeValidator.isValueValidForType("Tenor", "D0")); // Zero not allowed
        assertFalse(FieldTypeValidator.isValueValidForType("Tenor", "M"));  // Missing number
        assertFalse(FieldTypeValidator.isValueValidForType("Tenor", "13W")); // Wrong order
        assertFalse(FieldTypeValidator.isValueValidForType("Tenor", "Z5")); // Invalid prefix
    }
}
