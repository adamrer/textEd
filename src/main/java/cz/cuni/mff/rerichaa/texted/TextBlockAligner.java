package cz.cuni.mff.rerichaa.texted;
import java.io.*;

/**
 * Class aligning Tokens from ITokenReader by ITokenAligner.
 */
public class TextBlockAligner {
    /**
     * Joins a TokenReader and a TextAligner and aligns the lines to blocks to a BufferedWriter.
     * @param reader Reads Tokens for aligning.
     * @param aligner Aligns given Tokens to blocks.
     * @param writer Writes the aligned text.
     * @throws IOException Failed to read Tokens or writing Tokens.
     */
    public static void process(TokenReader reader, TextAligner aligner, BufferedWriter writer) throws IOException {
        Token token = reader.nextToken();
        while (token.type != TokenType.END_OF_INPUT) {
            aligner.processToken(token);

            String line = aligner.getAlignedLine();

            if (line != null) {
                writer.write(line);
            }

            token = reader.nextToken();
        }

        String lastLine = aligner.alignLineLeft();
        if (lastLine != null) {
            writer.write(lastLine);
        }
    }

}


