package cz.cuni.mff.rerichaa.texted;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that receives Tokens and WORD Tokens are aligned to the lineWidth
 */
public class TextAligner {
    private enum AlignerState { LINE_IS_FULL, END_OF_PARAGRAPH, FILLING_LINE }
    private AlignerState state;

    private final int lineWidth; // width of a line
    private int currLineWidth; // sum of lengths of words in lineWords
    private static final int MANDATORY_SPACE_COUNT = 1; // words have to be separated with this count of spaces
    private List<String> lineWords; // words on currently processing line
    private String nextLineWord; // word that doesn't fit on the line
    private boolean firstLineOfParagraph; // if the line should be aligned to the left

    /**
     * Creates a TextAligner that will align received WORD Tokens to lines with length of specified lineWidth.
     * @param lineWidth Width of the blocks.
     */
    public TextAligner(int lineWidth) {
        state = AlignerState.FILLING_LINE;
        this.lineWidth = lineWidth;
        currLineWidth = 0;
        lineWords = new ArrayList<>();
        nextLineWord = null;
        firstLineOfParagraph = false;
    }

    /**
     * Accepts Tokens. WORD Tokens are added to lineWords and currLineWidth is updated. If the token is END_OF_PARAGRAPH,
     * state is changed and line can be returned with getAlignedLine(). Other Token types are ignored.
     * @param token Token to be processed.
     */
    public void processToken(Token token) {
        switch (token.type) {
            case WORD:
                if (currLineWidth + token.value.length() + MANDATORY_SPACE_COUNT > lineWidth) {
                    state = AlignerState.LINE_IS_FULL;
                    nextLineWord = token.value;
                } else {
                    lineWords.add(token.value);
                    currLineWidth += token.value.length() + (lineWords.size() == 1 ? 0 : MANDATORY_SPACE_COUNT);
                }
                break;
            case END_OF_PARAGRAPH:
                state = AlignerState.END_OF_PARAGRAPH;
                break;
            default:
                break;
        }
    }

    /**
     * If TextAligner has enough words or is last line of paragraph, it returns an aligned line lineWidth long.
     * @return Aligned line with length of lineWidth or line aligned to left if it is last line of paragraph.
     * Null if line is still filling.
     */
    public String getAlignedLine() {
        String output = null;

        switch (state) {
            case LINE_IS_FULL:
                state = AlignerState.FILLING_LINE;
                output = (firstLineOfParagraph ? "\n" : "") + alignLineBlock();
                firstLineOfParagraph = false;
                break;
            case END_OF_PARAGRAPH:
                state = AlignerState.FILLING_LINE;
                String line = alignLineLeft();
                if (line != null) {
                    firstLineOfParagraph = true;
                    output = line;
                }
                break;
            case FILLING_LINE:
                break;
            default:
                break;
        }
        return output;
    }

    /**
     * Aligns words from lineWords to string with length of lineWidth. If the sum of lengths of the words in lineWords
     * is less than lineWidth, then it adds spaces from left to right.
     * @return String containing words from lineWords and aligned to have length of lineWidth.
     */
    private String alignLineBlock() {
        StringBuilder line = new StringBuilder(lineWidth + 1);
        int remain = lineWidth - currLineWidth;
        int spaceCountFillEverywhere = lineWords.size() == 1 ? 0 : remain / (lineWords.size() - 1);
        int spaceCountExtra = lineWords.size() == 1 ? 0 : remain % (lineWords.size() - 1);

        for (int i = 0; i < lineWords.size() - 1; i++) {
            int extra = spaceCountExtra-- > 0 ? 1 : 0;
            line.append(lineWords.get(i)).append(" ".repeat(MANDATORY_SPACE_COUNT + spaceCountFillEverywhere + extra));
        }
        line.append(lineWords.get(lineWords.size() - 1)).append('\n');

        getReadyForNextLine();
        return line.toString();
    }

    /**
     * Aligns words from lineWords to left to string.
     * @return Aligned line to left with words from lineWords. Null if lineWords has no words.
     */
    public String alignLineLeft() {
        if (lineWords.isEmpty()) {
            return null;
        }

        StringBuilder line = new StringBuilder(lineWidth + 1);
        for (int i = 0; i < lineWords.size() - 1; i++) {
            line.append(lineWords.get(i)).append(' ');
        }
        line.append(lineWords.get(lineWords.size() - 1)).append('\n');

        getReadyForNextLine();
        return line.toString();
    }

    /**
     * Prepares TextAligner to receive Tokens for next line.
     */
    private void getReadyForNextLine() {
        if (nextLineWord != null) {
            lineWords = new ArrayList<>(List.of(nextLineWord));
            currLineWidth = nextLineWord.length();
        } else {
            lineWords = new ArrayList<>();
            currLineWidth = 0;
        }
        nextLineWord = null;
    }
}
