package cz.cuni.mff.rerichaa.texted;


import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static cz.cuni.mff.rerichaa.texted.TextBlockAligner.process;

/**
 * Main class of text editor similar to linux text editor 'ed'. Loads text file to buffer, modifies the buffer and
 * writes to a file.
 */
public class TextEd {

    private static List<String> buffer = new ArrayList<>();
    private static List<String> prevBuffer = new ArrayList<>();
    /**
     * Used when file is not specified
     */
    private static String defaultFile = null;
    /**
     * Current line
     */
    private static int currLine;
    private static boolean showPrompt = false;
    /**
     * Show error messages
     */
    private static boolean showHelp = false;
    /**
     * Last error message
     */
    private static String lastError = "None";
    /**
     * Unsaved changes in the buffer
     */
    private static boolean changesMade = false;

    /**
     * Error messages
     */
    static final Hashtable<ErrorType, String> errorMessages = new Hashtable<>(){
        {
            put(ErrorType.ADDRESS, "Invalid address");
            put(ErrorType.UNKNOWN_COMMAND, "Unknown command");
            put(ErrorType.READING_FILE, "Error reading file");
            put(ErrorType.READING_INPUT, "Error reading input ");
            put(ErrorType.WRITING_FILE, "Error writing to file");
            put(ErrorType.WRITING_OUTPUT, "Error writing to output");
            put(ErrorType.UNSAVED, "Warning: Buffer modified");
            put(ErrorType.DEFAULT_FILE, "Default file not set");
            put(ErrorType.ARGUMENT, "Invalid argument");
            put(ErrorType.DELIMITER, "Missing pattern delimiter");
            put(ErrorType.REGEX, "Invalid syntax of regex");
            put(ErrorType.SUFFIX, "Invalid command suffix");
            put(ErrorType.NOMATCH, "No matches");
            put(ErrorType.TEXT_ALIGN, "Error aligning buffer");
        }
    };
    /**
     *  Known commands for TextEd
     */
    static final Hashtable<Character, CommandStructure> knownCommands = new Hashtable<>(){
        {//noDestination, noArgument
            //prompt
            put('P', new CommandStructure(true, true));
            //print lines
            put('p', new CommandStructure( true, true));
            //print lines with numbers
            put('n', new CommandStructure( true, true));
            //read
            put('r', new CommandStructure( true, false));
            //write to file
            put('w', new CommandStructure( true, false));
            //change current line
            put(' ', new CommandStructure(true, true));
            //change lines
            put('c', new CommandStructure(true, true));
            //delete lines
            put('d', new CommandStructure(true, true));
            //edit file
            put('e', new CommandStructure(true, false));
            // edit file
            put('E', new CommandStructure( true, false));
            //substitute regex
            put('s', new CommandStructure(true, true));
            //insert lines
            put('i', new CommandStructure( true, true));
            //append lines
            put('a', new CommandStructure( true, true));
            // join lines
            put('j', new CommandStructure(true, true));
            //copy lines to
            put('t', new CommandStructure(false, true));
            //quit
            put('q', new CommandStructure(true, true));
            //quit
            put('Q', new CommandStructure(true, true));
            //invalid command
            put('?', new CommandStructure( true, true));
            //show last error message
            put('h', new CommandStructure( true, true));
            // show error messages
            put('H', new CommandStructure(true, true));
            // change default file
            put('f', new CommandStructure(true, false));
            //undo last buffer changes
            put('u', new CommandStructure(true, true));
            // move lines to
            put('m', new CommandStructure(false, true));
            // align text to blocks
            put('b', new CommandStructure(true, false));

        }
    };

    /**
     * Main method where the main loop of the editor is located. Receives and executes
     * commands declared by the user.
     * @param args can be a file that will be set as default
     */
    public static void main(String[] args){
        buffer.add("");

        if (args.length >= 1){ // load file from argument
            defaultFile = args[0];
            readFile(defaultFile, 0);
        }

        try(BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) )){
            if (showPrompt) System.out.print("* ");
            String input = reader.readLine();

            // main loop
            while(!input.equals("Q")){
                if (!changesMade && input.equals("q"))
                    break;
                Command command = new Command(input, currLine, buffer.size()-1);

                if (checkStructure(command))
                    execute(command, reader);
                else
                    printError(ErrorType.UNKNOWN_COMMAND);
                if (showPrompt) System.out.print("* ");
                input = reader.readLine();

            }
        }
        catch (java.io.IOException e){
            printError(ErrorType.WRITING_OUTPUT);
        }


    }

    /**
     * Executes a command and its output prints to the standard output. Command name must be in knownCommands hashtable.
     * If it is not present, then print an error message. Execution is determined by the command name.
     * @param command Command that will be executed.
     * @param inputReader Input reader for commands where user input is needed.
     */
    public static void execute(Command command, BufferedReader inputReader){
        switch(command.name){
            case '?': // invalid command
                printError(command.error);
                break;
            case 'q': // quit if changes were saved
                if (command.range.state != RangeState.DEFAULT){
                    printError(ErrorType.ADDRESS);
                    break;
                }

                printError(ErrorType.UNSAVED);
                break;
            case 'h': // show last error message
                if (command.range.state != RangeState.DEFAULT){
                    printError(ErrorType.ADDRESS);
                    break;
                }
                System.out.println("? " + lastError);
                break;
            case 'H': // show/hide error messages

                if (command.range.state != RangeState.DEFAULT){
                    printError(ErrorType.ADDRESS);
                    return;
                }
                showHelp = !showHelp;
                break;
            case 'P':// show/hide prompt
                if (command.range.state != RangeState.DEFAULT){
                    printError(ErrorType.ADDRESS);
                    return;
                }
                showPrompt = !showPrompt;
                break;
            case 'f':// set default file or print current default file
                if (command.range.state != RangeState.DEFAULT){
                    printError(ErrorType.ADDRESS);
                    return;
                }
                if (command.argument == null && defaultFile != null){
                    System.out.println(defaultFile);
                    return;
                }
                else if (command.argument != null){
                    defaultFile = command.argument;
                    return;
                }
                else{
                    printError(ErrorType.DEFAULT_FILE);
                    return;
                }

            case 'r': // read file

                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(buffer.size()-1, buffer.size()-1);
                if (command.argument == null && defaultFile == null)
                    printError(ErrorType.DEFAULT_FILE);
                else if (command.argument != null){
                    saveBuffer();
                    readFile(command.argument, command.range.from);

                } else{
                    saveBuffer();
                    readFile(defaultFile, command.range.from);
                }
                break;
            case 'e': // edit file
                if (changesMade)
                    printError(ErrorType.UNSAVED);
                else{
                    saveBuffer();
                    editFile(command.argument);
                }

                break;
            case 'E':
                saveBuffer();

                editFile(command.argument);
                break;
            case 'p': // print lines
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                if (command.range.from <= 0){
                    printError(ErrorType.ADDRESS);
                    break;
                }
                printBuffer(command.range);
                break;
            case ' ': // change current line (number)
                if (command.range != null && command.range.state == RangeState.RANGE_SET){

                    int lineNumber = command.range.from;
                    if (lineNumber < buffer.size() && lineNumber > 0){
                        currLine = lineNumber;
                        printBuffer(new Range(currLine, currLine));
                    }
                    else
                        printError(ErrorType.ADDRESS);
                }
                else
                    printError(ErrorType.ADDRESS);
                break;

            case 'c': // change lines
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                if (command.range.from <= 0){
                    printError(ErrorType.ADDRESS);
                    break;
                }
                saveBuffer();

                deleteLines(command.range);
                appendLines(command.range.from-1, inputReader);
                break;
            case 'a': // append lines
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(buffer.size()-1, buffer.size()-1);
                appendLines(command.range.from, inputReader);
                saveBuffer();

                break;
            case 'i': // insert lines
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                if (command.range.from == 0)
                    command.range.from = 1;
                saveBuffer();

                appendLines(command.range.from-1, inputReader);
                break;
            case 'd': //delete lines
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                if (command.range.from <= 0){
                    printError(ErrorType.ADDRESS);
                    break;
                }
                saveBuffer();

                deleteLines(command.range);

                break;
            case 'n': // print lines with numbers
                if (command.range.state == RangeState.DEFAULT && buffer.size() > 1)
                    command.range = new Range(currLine, currLine);
                if (command.range.from <= 0){
                    printError(ErrorType.ADDRESS);
                    break;
                }

                printBufferNumbered(command.range);
                break;
            case 'w': // write buffer to file
                if ((command.argument == null || command.argument.isEmpty()) && !defaultFile.isEmpty())
                    writeBuffer(defaultFile, command.range);
                else if (command.argument != null && !command.argument.isEmpty())
                    writeBuffer(command.argument, command.range);
                else
                    printError(ErrorType.DEFAULT_FILE);
                break;
            case 't': // copy lines
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                if (command.destinationLine == null) {
                    command.destinationLine = currLine;
                }
                saveBuffer();

                copyLines(command.range, command.destinationLine);
                break;
            case 'm': // move lines
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                if (command.destinationLine == null) {
                    command.destinationLine = currLine;
                }
                saveBuffer();

                moveLines(command.range, command.destinationLine);
                break;
            case 'j': // join lines to one
                if (command.range.state == RangeState.DEFAULT) {
                    if (currLine +1 == buffer.size()){
                        printError(ErrorType.ADDRESS);
                        return;
                    }
                    command.range = new Range(currLine, currLine + 1);
                }
                joinLines(command.range);

                break;
            case 's': // substitute text found by regex
                saveBuffer();
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                substituteText(command.range, command.regex, command.replacement, command.suffixes);
                break;
            case 'b': // align text to blocks
                if (command.range.state != RangeState.DEFAULT){
                    printError(ErrorType.ADDRESS);
                    return;
                }
                try{
                    saveBuffer();
                    if (command.argument == null || command.argument.isEmpty())
                        alignBuffer(72);
                    else
                        alignBuffer(Integer.parseInt(command.argument));
                }
                catch(NumberFormatException e){
                    printError(ErrorType.ARGUMENT);
                }
                break;
            case 'u': // undo changes on buffer, get buffer to the previous state
                if (command.range.state != RangeState.DEFAULT){
                    printError(ErrorType.ADDRESS);
                    return;
                }
                else if (command.destinationLine != null) {
                    printError(ErrorType.DESTINATION);
                    return;
                }
                loadPreviousBuffer();
                break;

            default:
                printError(ErrorType.UNKNOWN_COMMAND);
                break;


        }

    }

    /**
     * Substitutes text determined by regular expression in lines in the buffer.
     * Last substituted line is printed to standard output
     * @param range Range where the command will search for text matching the regex
     * @param regex Regular expression describing the text you want to substitute
     * @param replacement Text that will replace the text matching the regex
     * @param suffixes Additional suffixes:
     *                 g (global) - replace all matches with replacement
     *                 n (number) - print last matched line with index number
     *                 I, i (insensitive) - matching text is case-insensitive
     */
    private static void substituteText(Range range, String regex, String replacement, String suffixes){
        boolean global = suffixes.contains("g");
        boolean number = suffixes.contains("n");
        boolean caseInsensitive = suffixes.contains("i") || suffixes.contains("I");

        Integer matchedLine = null;
        Pattern pattern;
        try{
            pattern = Pattern.compile(regex);
        }
        catch (PatternSyntaxException e){
            printError(ErrorType.REGEX);
            return;
        }

        for (int i = range.from; i <= range.to; i++){
            String line = buffer.get(i);
            if (caseInsensitive){
                line = line.toLowerCase();
                regex = regex.toLowerCase();
            }
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()){
                matchedLine = i;

                if (global)
                    buffer.set(i, line.replaceAll(regex, replacement));
                else {
                    buffer.set(i, line.replaceFirst(regex, replacement));
                    break;
                }
            }
        }
        Range suffixRange;
        if (matchedLine != null) {
            changesMade = true;
            suffixRange = new Range(matchedLine, matchedLine);
            if (number)
                printBufferNumbered(suffixRange);
            else
                printBuffer(suffixRange);
        } else
            printError(ErrorType.NOMATCH);
    }

    /**
     * Aligns the text in the buffer to specified line width.
     * If the line is the last line of the paragraph, aligns it to
     * the left.
     * @param lineWidth Count of characters on one line
     */
    private static void alignBuffer(int lineWidth){
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 1; i < buffer.size(); i++){

            stringBuffer.append(buffer.get(i)).append('\n');
        }
        StringWriter result = new StringWriter();
        try (PushbackReader reader = new PushbackReader(new StringReader(stringBuffer.toString()));
             BufferedWriter writer = new BufferedWriter(result);){

            TokenReader tokenReader = new TokenReader(reader);
            TextAligner tokenProcessor = new TextAligner(lineWidth);

            process(tokenReader, tokenProcessor, writer);

        } catch (IOException e) {
            printError(ErrorType.TEXT_ALIGN);
            return;
        }

        String[] resultLines = result.toString().split("\\n");
        if (resultLines.length < buffer.size()){
            deleteLines(new Range(resultLines.length, buffer.size()-1));
        }
        for (int i = 0; i < resultLines.length; i++){
            if (i+1 >= buffer.size())
                buffer.add(resultLines[i]);
            else
                buffer.set(i+1, resultLines[i]);
        }
        currLine = buffer.size()-1;

    }

    /**
     * Moves lines from buffer in specified range to the destination index. If the destination index is too big
     * then it moves lines on the end of the buffer.
     * @param range Line indices which will be moved.
     * @param destination Line index, where the lines will be moved.
     */
    private static void moveLines(Range range, int destination){
        List<String> copy = new ArrayList<>();
        for(int i = range.from; i <= range.to; i++){
            copy.add(buffer.get(i));
        }
        deleteLines(range);
        int appendIndex = Math.min(destination, buffer.size());
        for (int i = appendIndex; i < appendIndex + copy.size(); i++){
            buffer.add(i, copy.get(i-appendIndex));
        }
        currLine = destination + copy.size() -1;
    }
    /**
     * Prints lines from buffer in range with index numbers.
     * @param range Range of line indices
     */
    private static void printBufferNumbered(Range range){
        for (int i = range.from; i <= range.to; i++){
            System.out.println(i + "\t" + buffer.get(i));
        }
    }

    /**
     * Saves current buffer prevBuffer. Can be loaded with loadPreviousBuffer()
     */
    private static void saveBuffer(){
        changesMade = true;

        prevBuffer = new ArrayList<>(buffer);
    }

    /**
     * Loads lines from prevBuffer saved in the past.
     */
    private static void loadPreviousBuffer(){
        changesMade = true;

        List<String> temp = new ArrayList<>(buffer);
        buffer = prevBuffer;
        prevBuffer = temp;
    }

    /**
     * Copies lines in range from buffer and appends them after destination.
     * @param range Range of line indices which will be copied.
     * @param destination Destination index where copied lines will be inserted.
     */
    private static void copyLines(Range range, int destination){
        changesMade = true;

        List<String> copy = new ArrayList<>();
        for (int i = range.from; i <= range.to; i++){
            copy.add(buffer.get(i));
        }
        for (int i = range.to; i >= range.from; i--){
            buffer.add(destination, copy.get(i-range.from));
        }
    }

    /**
     * Concatenates lines from buffer in range into a single line.
     * @param range Range of line indices to be joined together.
     */
    private static void joinLines(Range range){
        changesMade = true;

        for (int i = range.from+1; i <= range.to; i++){
            buffer.set(range.from, buffer.get(range.from) + buffer.get(i));
        }
        deleteLines(new Range(range.from+1, range.to));
    }

    /**
     * Deletes buffer and reads specified file. If file not specified, default file will be used.
     * If file is specified, it is set as default file.
     * @param file File from which to read.
     */
    private static void editFile(String file){
        changesMade = true;

        buffer = new ArrayList<>();
        buffer.add("");
        readFile(file, 0);
    }

    /**
     * Deletes lines from buffer in specified range.
     * @param range Range of line indices which will be deleted
     */
    private static void deleteLines(Range range){
        if (range.to >= range.from) {
            buffer.subList(range.from, range.to + 1).clear();
        }
        currLine = Math.min(range.to, buffer.size() - 1);

        changesMade = true;
    }

    /**
     * Prints error message from errorMessages hashtable determined by given ErrorType.
     * @param error Type of the printed error.
     */
    private static void printError(ErrorType error){
        lastError = errorMessages.get(error);
        if (showHelp)
            System.out.println("? " + errorMessages.get(error));
        else
            System.out.println("?");
    }

    /**
     * Appends lines typed to the inputReader after the specified index.
     * @param after After this line index will be appended typed lines.
     * @param inputReader Reader in which the user will type the lines that will be appended.
     */
    private static void appendLines(int after, BufferedReader inputReader){
        int index = after;
        try{
            String line = inputReader.readLine();
            while(!line.equals(".")){
                buffer.add(index+1, line);
                index++;
                line = inputReader.readLine();
            }
        }catch(java.io.IOException e){
            printError(ErrorType.READING_INPUT);
        }

        currLine = index - 1;
        changesMade = true;
    }

    /**
     * Reads all lines from specified file and loads them to the buffer. Prints the count of characters read.
     * @param filePath File for reading lines. If not specified, default file will be used. If specified, file will be set
     *                 as default.
     * @param appendAfter Read lines are appended after this line index.
     */
    private static void readFile(String filePath, int appendAfter){//
        if (filePath == null)
            filePath = defaultFile;
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){

            String line = reader.readLine();
            int charSum = 0;

            int i = appendAfter;
            while(line != null){
                buffer.add(i + 1, line);
                i++;
                charSum += line.length();
                line = reader.readLine();
            }


            if (buffer.size() > 1){
                currLine = buffer.size()-1;
            }
            defaultFile = filePath;
            System.out.println(charSum);
        }
        catch (IOException e){
            printError(ErrorType.READING_FILE);
        }
    }

    /**
     * Prints the specified range of lines from buffer.
     * @param range Range of line indices to print.
     */
    private static void printBuffer(Range range){

        for( int i = range.from; i <= range.to; i++){
            System.out.println(buffer.get(i));
        }
    }

    /**
     * Overwrites the given file with lines from buffer. Prints the count of characters written to the file.
     * @param filePath File to write the lines. File will be set as default if specified. If not, default file will be used.
     * @param range Range of line indices that will be written to the specified file.
     */
    private static void writeBuffer(String filePath, Range range) {

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false))){
            if (range.state == RangeState.DEFAULT){
                range = new Range(1, buffer.size()-1);
            }
            int charSum = 0;
            for (int i = range.from; i <= range.to; i++) {
                charSum += buffer.get(i).length();
                writer.write(buffer.get(i) + "\n");
            }
            System.out.println(charSum);

        }catch(java.io.IOException e){
            printError(ErrorType.WRITING_FILE);
        }
        changesMade = false;

    }

    /**
     * Determines if given command satisfies command structure set in knownCommands.
     * @param command Command to check.
     * @return True if command satisfies its command structure, otherwise false.
     */
    public static boolean checkStructure(Command command){

        if (!knownCommands.containsKey(command.name)){
            return false;
        }
        CommandStructure structure = knownCommands.get(command.name);
        return (!structure.noDestination() || command.destinationLine == null) &&
                (!structure.noArgument() || command.argument == null || command.argument.isEmpty());
    }

}
