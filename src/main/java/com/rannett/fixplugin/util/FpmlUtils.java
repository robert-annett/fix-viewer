package com.rannett.fixplugin.util;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;

/**
 * Utility methods related to detecting FpML content in FIX messages.
 */
public final class FpmlUtils {
    private FpmlUtils() {}

    /**
     * Determine whether the given text appears to be valid FpML XML.
     * This performs a lightweight check by attempting to parse the
     * text as XML and looking for an "FpML" root element.
     *
     * @param text candidate text
     * @return true if the text looks like FpML, false otherwise
     */
    public static boolean isLikelyFpml(String text) {
        if (text == null) {
            return false;
        }

        String trimmed = text.trim();
        if (!trimmed.startsWith("<")) {
            return false;
        }

        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(trimmed.getBytes()));
            String root = doc.getDocumentElement().getNodeName();
            return root != null && root.toLowerCase().contains("fpml");
        } catch (Exception e) {
            return false;
        }
    }
}
