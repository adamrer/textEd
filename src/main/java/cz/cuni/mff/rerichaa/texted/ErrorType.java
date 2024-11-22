package cz.cuni.mff.rerichaa.texted;

/**
 * Types of TextEd errors
 */
public enum ErrorType {
    /**
     * Invalid address
     */
    ADDRESS,
    /**
     * Invalid destination line index
     */
    DESTINATION,
    /**
     * Command is not known by the TextEd
     */
    UNKNOWN_COMMAND,
    /**
     * Error while reading a file
     */
    READING_FILE,
    /**
     * Error while writing to a file
     */
    WRITING_FILE,
    /**
     * Error while reading from input
     */
    READING_INPUT,
    /**
     * Error while writing to output
     */
    WRITING_OUTPUT,
    /**
     * Warning that TextEd buffer was modified and changes were not written to file
     */
    UNSAVED,
    /**
     * Default file is not set. Missing file argument
     */
    DEFAULT_FILE,
    /**
     * Invalid argument for the command
     */
    ARGUMENT,
    /**
     * Delimiter is missing
     */
    DELIMITER,
    /**
     * Invalid syntax of given regular expression
     */
    REGEX,
    /**
     * Invalid suffix
     */
    SUFFIX,
    /**
     * No match was found for the given regular expression
     */
    NOMATCH,
    /**
     * Error while aligning text
     */
    TEXT_ALIGN

}
