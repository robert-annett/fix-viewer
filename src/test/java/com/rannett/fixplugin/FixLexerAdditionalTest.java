package com.rannett.fixplugin;

import com.rannett.fixplugin.psi.FixTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FixLexerAdditionalTest {

    @Test
    public void testLexerTokenSequence() throws Exception {
        String msg = "35=A|10=999|";
        FixLexer lexer = new FixLexer(new StringReader(msg));
        lexer.reset(msg, 0, msg.length(), FixLexer.YYINITIAL);

        List<IElementType> tokens = new ArrayList<>();
        IElementType token;
        while ((token = lexer.advance()) != null) {
            tokens.add(token);
        }

        List<IElementType> expected = List.of(
                FixTypes.TAG,
                FixTypes.SEPARATOR,
                FixTypes.VALUE,
                FixTypes.FIELD_SEPARATOR,
                FixTypes.TAG,
                FixTypes.SEPARATOR,
                FixTypes.VALUE,
                FixTypes.FIELD_SEPARATOR
        );
        assertEquals(expected, tokens);
    }

    @Test
    public void testLexerHandlesCommentsAndWhitespace() throws Exception {
        String msg = "#comment\n8=FIX.4.2|10=000|";
        FixLexer lexer = new FixLexer(new StringReader(msg));
        lexer.reset(msg, 0, msg.length(), FixLexer.YYINITIAL);

        List<IElementType> tokens = new ArrayList<>();
        IElementType token;
        while ((token = lexer.advance()) != null) {
            tokens.add(token);
        }

        List<IElementType> expected = List.of(
                FixTypes.TAG,          // comment returned as TAG
                TokenType.WHITE_SPACE, // newline
                FixTypes.TAG,
                FixTypes.SEPARATOR,
                FixTypes.VALUE,
                FixTypes.FIELD_SEPARATOR,
                FixTypes.TAG,
                FixTypes.SEPARATOR,
                FixTypes.VALUE,
                FixTypes.FIELD_SEPARATOR
        );
        assertEquals(expected, tokens);
    }
}
