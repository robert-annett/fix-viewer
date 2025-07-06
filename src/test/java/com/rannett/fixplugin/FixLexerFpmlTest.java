package com.rannett.fixplugin;

import com.rannett.fixplugin.FixLexer;
import com.rannett.fixplugin.psi.FixTypes;
import com.intellij.psi.tree.IElementType;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class FixLexerFpmlTest {
    @Test
    public void testLexerHandlesFpmlWithNewlines() throws Exception {
        String fpml = "<FpML>\n<trade>test</trade>\n</FpML>";
        String msg = "8=FIX.4.4|9=70|35=AE|350=" + fpml.length() + "|351=" + fpml + "|10=000|";
        FixLexer lexer = new FixLexer(new StringReader(msg));
        lexer.reset(msg, 0, msg.length(), FixLexer.YYINITIAL);
        String lastTag = null;
        String fpmlValue = null;
        IElementType token;
        while ((token = lexer.advance()) != null) {
            String text = lexer.yytext().toString();
            if (token == FixTypes.TAG) {
                lastTag = text;
            } else if (token == FixTypes.VALUE && "351".equals(lastTag)) {
                fpmlValue = text;
            }
        }
        assertEquals(fpml, fpmlValue);
    }
}
