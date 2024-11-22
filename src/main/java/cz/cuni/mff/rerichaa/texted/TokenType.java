package cz.cuni.mff.rerichaa.texted;

/**
 * Describing the types that Token can be.
 */
public enum TokenType {
    /**
     * String separated by white characters, end of input or start of input
     */
    WORD,
    /**
     * One end of line between white characters and WORDs
     */
    END_OF_LINE,
    /**
     * Two ends of lines where there are no WORDS between them
     */
    END_OF_PARAGRAPH,
    /**
     * End of input
     */
    END_OF_INPUT }

