package com.rannett.fixplugin.annotator;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class FixCheckTypeAnnotatorTest extends BasePlatformTestCase {

    // The FIX lexer accepts | as a field delimiter in test files (same as in FixInvalidCharAnnotatorTest).
    private static final String SEP = "|";

    // A minimal FIX.4.2 message wrap so the annotator can extract the version.
    private static final String PREFIX = "8=FIX.4.2" + SEP;
    private static final String SUFFIX = "10=000" + SEP;

    // -------------------------------------------------------------------------
    // Valid values — no errors expected
    // -------------------------------------------------------------------------

    public void testValidIntValue_noAnnotation() {
        // tag 34 (MsgSeqNum) has type INT
        String message = PREFIX + "34=1" + SEP + SUFFIX;
        myFixture.configureByText("test.fix", message);
        boolean hasTypeError = myFixture.doHighlighting().stream()
                .anyMatch(info -> info.getSeverity() == HighlightSeverity.ERROR
                        && info.getDescription() != null
                        && info.getDescription().startsWith("Invalid value for type"));
        assertFalse("A valid INT value should produce no type error", hasTypeError);
    }

    public void testValidStringValue_noAnnotation() {
        // tag 35 (MsgType) has type STRING — any value is valid
        String message = PREFIX + "35=D" + SEP + SUFFIX;
        myFixture.configureByText("test.fix", message);
        boolean hasTypeError = myFixture.doHighlighting().stream()
                .anyMatch(info -> info.getSeverity() == HighlightSeverity.ERROR
                        && info.getDescription() != null
                        && info.getDescription().startsWith("Invalid value for type"));
        assertFalse("A valid STRING value should produce no type error", hasTypeError);
    }

    public void testValidNegativeIntValue_noAnnotation() {
        // tag 34 INT; negative integers are valid per FieldTypeValidator
        String message = PREFIX + "34=-5" + SEP + SUFFIX;
        myFixture.configureByText("test.fix", message);
        boolean hasTypeError = myFixture.doHighlighting().stream()
                .anyMatch(info -> info.getSeverity() == HighlightSeverity.ERROR
                        && info.getDescription() != null
                        && info.getDescription().startsWith("Invalid value for type"));
        assertFalse("A negative INT value should produce no type error", hasTypeError);
    }

    // -------------------------------------------------------------------------
    // Invalid values — errors expected
    // -------------------------------------------------------------------------

    public void testInvalidIntValue_producesError() {
        // tag 34 (MsgSeqNum) has type INT; "ABC" is not a valid integer
        String message = PREFIX + "34=ABC" + SEP + SUFFIX;
        myFixture.configureByText("test.fix", message);
        boolean hasTypeError = myFixture.doHighlighting().stream()
                .anyMatch(info -> info.getSeverity() == HighlightSeverity.ERROR
                        && info.getDescription() != null
                        && info.getDescription().startsWith("Invalid value for type INT"));
        assertTrue("A non-integer value for an INT field should produce a type error", hasTypeError);
    }

    public void testInvalidIntValue_errorMessageContainsExpectedType() {
        String message = PREFIX + "34=NOTANINT" + SEP + SUFFIX;
        myFixture.configureByText("test.fix", message);
        boolean found = myFixture.doHighlighting().stream()
                .anyMatch(info -> info.getSeverity() == HighlightSeverity.ERROR
                        && info.getDescription() != null
                        && info.getDescription().contains("INT"));
        assertTrue("Error annotation should name the expected FIX type", found);
    }

    // -------------------------------------------------------------------------
    // Unknown tags — no errors (no type in dictionary → no annotation)
    // -------------------------------------------------------------------------

    public void testUnknownTag_noTypeAnnotation() {
        // tag 99999 is not in the FIX.4.2 dictionary
        String message = PREFIX + "99999=ANYTHING" + SEP + SUFFIX;
        myFixture.configureByText("test.fix", message);
        boolean hasTypeError = myFixture.doHighlighting().stream()
                .anyMatch(info -> info.getSeverity() == HighlightSeverity.ERROR
                        && info.getDescription() != null
                        && info.getDescription().startsWith("Invalid value for type"));
        assertFalse("Unknown tags have no type constraint and should not trigger a type error", hasTypeError);
    }

    // -------------------------------------------------------------------------
    // Non-VALUE elements — annotator must skip TAG tokens
    // -------------------------------------------------------------------------

    public void testTagElement_isNotAnnotatedAsTypeError() {
        // The annotator only fires on VALUE element types; TAG tokens must be ignored.
        // A file with an invalid INT value on tag 34 should produce exactly one error
        // (on the value), not two (one on the tag as well).
        String message = PREFIX + "34=BAD" + SEP + SUFFIX;
        myFixture.configureByText("test.fix", message);
        long typeErrors = myFixture.doHighlighting().stream()
                .filter(info -> info.getSeverity() == HighlightSeverity.ERROR
                        && info.getDescription() != null
                        && info.getDescription().startsWith("Invalid value for type"))
                .count();
        assertEquals("Exactly one type-error annotation expected (on the value, not the tag)", 1, typeErrors);
    }

    // -------------------------------------------------------------------------
    // Version extraction — file without BeginString falls back to FIXT.1.1
    // -------------------------------------------------------------------------

    public void testFileWithoutVersion_fallsBackGracefully() {
        // No tag 8 present; annotator must not throw and should apply FIXT.1.1 rules
        String message = "34=ABC" + SEP + "10=000" + SEP;
        myFixture.configureByText("test.fix", message);
        // Just verify no exception is thrown and highlighting completes
        assertNotNull(myFixture.doHighlighting());
    }
}
