package com.rannett.fixplugin.dictionary;

/**
 * Enumeration of the sections within a FIX message where a field may appear.
 */
public enum FieldSection {
    /** Field appears in the message header. */
    HEADER,
    /** Field appears in the message body. */
    BODY,
    /** Field appears in the message trailer. */
    TRAILER
}

