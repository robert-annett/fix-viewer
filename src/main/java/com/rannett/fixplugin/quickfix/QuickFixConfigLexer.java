package com.rannett.fixplugin.quickfix;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lexer for QuickFIX session configuration files.
 */
public class QuickFixConfigLexer extends LexerBase {

    private CharSequence buffer;
    private int bufferEnd;
    private int tokenStart;
    private int tokenEnd;
    private IElementType tokenType;

    /**
     * Creates a new lexer instance.
     */
    public QuickFixConfigLexer() {
        this.buffer = "";
    }

    /**
     * Initializes the lexer with a new buffer.
     *
     * @param buffer the buffer to lex.
     * @param startOffset the start offset.
     * @param endOffset the end offset.
     * @param initialState the initial lexer state.
     */
    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.bufferEnd = endOffset;
        this.tokenStart = startOffset;
        this.tokenEnd = startOffset;
        this.tokenType = null;
        advance();
    }

    /**
     * Returns the current lexer state.
     *
     * @return the lexer state.
     */
    @Override
    public int getState() {
        return 0;
    }

    /**
     * Returns the current token type.
     *
     * @return the token type.
     */
    @Nullable
    @Override
    public IElementType getTokenType() {
        return tokenType;
    }

    /**
     * Returns the start offset of the current token.
     *
     * @return the start offset.
     */
    @Override
    public int getTokenStart() {
        return tokenStart;
    }

    /**
     * Returns the end offset of the current token.
     *
     * @return the end offset.
     */
    @Override
    public int getTokenEnd() {
        return tokenEnd;
    }

    /**
     * Advances to the next token.
     */
    @Override
    public void advance() {
        tokenStart = tokenEnd;
        locateToken();
    }

    /**
     * Returns the buffer sequence.
     *
     * @return the buffer sequence.
     */
    @NotNull
    @Override
    public CharSequence getBufferSequence() {
        return buffer;
    }

    /**
     * Returns the end offset of the buffer.
     *
     * @return the buffer end offset.
     */
    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }

    private void locateToken() {
        if (tokenStart >= bufferEnd) {
            tokenType = null;
            return;
        }

        char currentChar = buffer.charAt(tokenStart);
        if (Character.isWhitespace(currentChar)) {
            int offset = tokenStart;
            while (offset < bufferEnd && Character.isWhitespace(buffer.charAt(offset))) {
                offset++;
            }
            tokenEnd = offset;
            tokenType = TokenType.WHITE_SPACE;
            return;
        }

        int lineStart = findLineStart(tokenStart);
        int lineEnd = findLineEnd(tokenStart);
        int firstNonWhitespace = findFirstNonWhitespace(lineStart, lineEnd);

        if (tokenStart < firstNonWhitespace) {
            tokenEnd = firstNonWhitespace;
            tokenType = TokenType.WHITE_SPACE;
            return;
        }

        if (tokenStart == firstNonWhitespace) {
            if (isCommentStart(tokenStart, lineEnd)) {
                tokenEnd = lineEnd;
                tokenType = QuickFixConfigTokenType.COMMENT;
                return;
            }

            if (currentChar == '[') {
                int closingBracket = indexOf(']', tokenStart + 1, lineEnd);
                if (closingBracket != -1) {
                    tokenEnd = closingBracket + 1;
                    tokenType = QuickFixConfigTokenType.SECTION;
                    return;
                }
            }
        }

        int equalsIndex = indexOf('=', lineStart, lineEnd);
        if (equalsIndex != -1) {
            if (tokenStart < equalsIndex) {
                tokenEnd = equalsIndex;
                tokenType = QuickFixConfigTokenType.KEY;
                return;
            }

            if (tokenStart == equalsIndex) {
                tokenEnd = tokenStart + 1;
                tokenType = QuickFixConfigTokenType.SEPARATOR;
                return;
            }

            if (tokenStart > equalsIndex) {
                tokenEnd = lineEnd;
                tokenType = QuickFixConfigTokenType.VALUE;
                return;
            }
        }

        tokenEnd = lineEnd;
        tokenType = QuickFixConfigTokenType.VALUE;
    }

    private int findLineStart(int offset) {
        int start = offset;
        while (start > 0 && buffer.charAt(start - 1) != '\n') {
            start--;
        }
        return start;
    }

    private int findLineEnd(int offset) {
        int end = offset;
        while (end < bufferEnd && buffer.charAt(end) != '\n') {
            end++;
        }
        return end;
    }

    private int findFirstNonWhitespace(int start, int end) {
        int offset = start;
        while (offset < end && Character.isWhitespace(buffer.charAt(offset)) && buffer.charAt(offset) != '\n') {
            offset++;
        }
        return offset;
    }

    private boolean isCommentStart(int offset, int lineEnd) {
        char currentChar = buffer.charAt(offset);
        if (currentChar == '#' || currentChar == ';') {
            return true;
        }
        if (currentChar == '/' && offset + 1 < lineEnd && buffer.charAt(offset + 1) == '/') {
            return true;
        }
        return false;
    }

    private int indexOf(char target, int start, int end) {
        int offset = start;
        while (offset < end) {
            if (buffer.charAt(offset) == target) {
                return offset;
            }
            offset++;
        }
        return -1;
    }
}
