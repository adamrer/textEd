package cz.cuni.mff.rerichaa.texted;

/**
 * Class representing a string token. If type is not WORD, value should be empty string.
 */
public class Token {
    public final TokenType type;
    public final String value;

    /**
     * Creates a Token with the specified type and value.
     * @param type Type of the Token.
     * @param value String value of the Token. Should be empty string if the type is not a WORD.
     */
    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }
}
