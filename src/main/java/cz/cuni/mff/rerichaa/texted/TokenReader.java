package cz.cuni.mff.rerichaa.texted;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.List;

/**
 * Class for translating text to Tokens and reading them.
 */
public class TokenReader {
    private final PushbackReader reader;
    private final List<Character> whiteSpaceChars;

    /**
     * Creates a TokenReader, that translates text from a PushbackReader to Tokens.
     * @param reader PushbackReader from which will be read.
     */
    public TokenReader(PushbackReader reader) {
        this.reader = reader;
        whiteSpaceChars = List.of('\r', '\t', ' ');
    }

    /**
     * Reads characters from reader and returns the next Token.
     * @return Next Token
     * @throws IOException If there is problem reading characters from the reader.
     */
    public Token nextToken() throws IOException {

        skipWhiteChars();

        int intChar = reader.read();
        if (intChar < 0) return new Token(TokenType.END_OF_INPUT, "");

        int paragraphCountdown = 2;
        while (intChar >= 0 && (char) intChar == '\n') {
            if (paragraphCountdown != 0) paragraphCountdown--;
            skipWhiteChars();
            intChar = reader.read();
        }
        if (intChar < 0) return new Token(TokenType.END_OF_INPUT, "");

        if (paragraphCountdown == 0) {
            reader.unread(intChar);
            return new Token(TokenType.END_OF_PARAGRAPH, "");
        }
        else if (paragraphCountdown == 1) {
            reader.unread(intChar);
            return new Token(TokenType.END_OF_LINE, "");
        }

        StringBuilder word = new StringBuilder();
        while (intChar >= 0 && !whiteSpaceChars.contains((char) intChar) && (char) intChar != '\n') {
            word.append((char) intChar);
            intChar = reader.read();
        }
        if ((char) intChar == '\n')
            reader.unread('\n');
        return new Token(TokenType.WORD, word.toString());
    }

    /**
     * Skips white characters described in whiteSpaceChars. Not skipping new line characters.
     * @throws IOException If there is problem reading characters from the reader.
     */
    private void skipWhiteChars() throws IOException {
        int intChar = reader.read();
        while (intChar >= 0 && whiteSpaceChars.contains((char) intChar)) {
            intChar = reader.read();
        }
        if (intChar >= 0) {
            reader.unread(intChar);
        }
    }
}

