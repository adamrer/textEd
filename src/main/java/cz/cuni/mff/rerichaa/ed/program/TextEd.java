package cz.cuni.mff.rerichaa.ed.program;


import cz.cuni.mff.rerichaa.ed.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class TextEd {

    private static List<String> buffer = new ArrayList<>();
    private static List<String> prevBuffer = new ArrayList<>();
    private static String defaultFile; // used when file is not specified
    private static int currLine; // current line
    private static boolean showPrompt = false;
    private static boolean showHelp = false; // show error messages
    private static String lastError = null;
    private static boolean changesMade = false; // unsaved changes in the buffer


    static Hashtable<ErrorType, String> errorMessages = new Hashtable<>(){
        {
            put(ErrorType.ADDRESS, "Invalid address");
            put(ErrorType.UNKNOWNCOMMAND, "Unknown command");
            put(ErrorType.READINGFILE, "Error reading file");
            put(ErrorType.READINGINPUT, "Error reading input ");
            put(ErrorType.WRITINGFILE, "Error writing to file");
            put(ErrorType.WRITINGOUTPUT, "Error writing to output");
            put(ErrorType.UNSAVED, "Warning: Buffer has unsaved changes");
            put(ErrorType.DEFAULTFILE, "Default file not set");
            put(ErrorType.ARGUMENT, "Invalid argument");
            put(ErrorType.REGEX, "Missing pattern delimiter");
        }
    };
    static Hashtable<Character, CommandStructure> knownCommands = new Hashtable<>(){
        {//noDestination, noArgument
            //prompt
            put('P', new CommandStructure(true, true));
            //print
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

        }
    };

    private static void initialize(){
        buffer = new ArrayList<>();
        buffer.add("");
        currLine = 0;
        defaultFile = null;
        showPrompt = false;
        showHelp = false;
        prevBuffer = new ArrayList<>();

    }
    public static void main(String[] args){
        initialize();
        if (args.length >= 1){
            defaultFile = args[0];
            readFile(defaultFile, new Range(RangeState.DEFAULT));
        }


        System.out.println("Working Directory = " + System.getProperty("user.dir"));
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
//                    System.out.println(command);
                else printError(ErrorType.UNKNOWNCOMMAND);

                if (showPrompt) System.out.print("* ");
                input = reader.readLine();

            }
        }
        catch (java.io.IOException e){
            printError(ErrorType.WRITINGOUTPUT);
        }


    }
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
                System.out.println("! " + lastError);
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
                    printError(ErrorType.DEFAULTFILE);
                    return;
                }

            case 'r': // read file

                if (command.argument == null && defaultFile == null)
                    printError(ErrorType.DEFAULTFILE);
                else if (command.argument != null){
                    saveBuffer();
                    readFile(command.argument, command.range);

                } else{
                    saveBuffer();
                    readFile(defaultFile, command.range);
                }
                break;
            case 'e': // edit file
                if (changesMade){
                    printError(ErrorType.UNSAVED);
                }
                else{
                    saveBuffer();

                    editFile(command.range, command.argument);
                }

                break;
            case 'E':
                saveBuffer();

                editFile(command.range, command.argument);
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
                if (command.range != null && command.range.state == RangeState.SETRANGE){

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

                for (int i = command.range.from; i <= command.range.to; i++){
                    System.out.println(i + "\t" + buffer.get(i));
                }
                break;
            case 'w': // write buffer to file
                if ((command.argument == null || command.argument.isEmpty()) && !defaultFile.isEmpty())
                    writeBuffer(defaultFile, command.range);
                else if (command.argument != null && !command.argument.isEmpty())
                    writeBuffer(command.argument, command.range);
                else
                    printError(ErrorType.DEFAULTFILE);
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
//                else if (command.argument)
                loadPreviousBuffer();
                break;

            default:
                printError(ErrorType.UNKNOWNCOMMAND);
                break;


        }

    }

    private static void substituteText(Range range, String regex, String replacement){
        for (int i = range.from; i <= range.to; i++){

        }
    }
    private static void saveBuffer(){
        prevBuffer = new ArrayList<>(buffer);
    }
    private static void loadPreviousBuffer(){
        List<String> temp = new ArrayList<>(buffer);
        buffer = prevBuffer;
        prevBuffer = temp;
    }
    private static void copyLines(Range range, int destination){
        List<String> copy = new ArrayList<>();
        for (int i = range.from; i <= range.to; i++){
            copy.add(buffer.get(i));
        }
        for (int i = range.to; i >= range.from; i--){
            buffer.add(destination, copy.get(i-range.from));
        }
    }
    private static void joinLines(Range range){
        for (int i = range.from+1; i <= range.to; i++){
            buffer.set(range.from, buffer.get(range.from) + buffer.get(i));
        }
        deleteLines(new Range(range.from+1, range.to));
    }
    private static void editFile(Range range, String file){
        buffer = new ArrayList<>();
        buffer.add("");
        if (range.from < 0){
            printError(ErrorType.ADDRESS);
            return;
        }
        readFile(file, range);
    }
    private static void deleteLines(Range range){
        if (range.to >= range.from) {
            buffer.subList(range.from, range.to + 1).clear();
        }
        currLine = Math.min(range.to, buffer.size() - 1);

        changesMade = true;
    }
    private static void printError(ErrorType error){
        lastError = errorMessages.get(error);
        if (showHelp)
            System.out.println("? " + errorMessages.get(error));
        else
            System.out.println("?");
    }
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
            printError(ErrorType.READINGINPUT);
        }

        currLine = index - 1;
        changesMade = true;
    }
    private static void readFile(String filePath, Range range){
        if (filePath == null)
            filePath = defaultFile;
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){

            String line = reader.readLine();
            int charSum = 0;

            if (range.state == RangeState.DEFAULT){
                while(line != null){
                    buffer.add(line);
                    charSum += line.length();
                    line = reader.readLine();
                }
            }else{
                int currentLine = range.from;
                int finalLine= range.to;
                while(currentLine <= finalLine){
                    buffer.add(line);
                    charSum += line.length();
                    currentLine++;
                    line = reader.readLine();
                }
            }

            if (buffer.size() > 1){
                currLine = buffer.size()-1;
            }
            defaultFile = filePath;
            System.out.println(charSum);
        }
        catch (IOException e){
            printError(ErrorType.READINGFILE);
        }
    }

    private static void printBuffer(Range range){

        for( int i = range.from; i <= range.to; i++){
            System.out.println(buffer.get(i));
        }
    }

    private static void writeBuffer(String filePath, Range range) {

        if (range != null && (range.to >= buffer.size() || range.from < 0 || range.to < 0)){
            System.out.println("! Out of range");
            return;
        }

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
            printError(ErrorType.WRITINGFILE);
        }
        changesMade = false;

    }

    public static boolean checkStructure(Command command){

        if (!knownCommands.containsKey(command.name)){
            return false;
        }
        CommandStructure structure = knownCommands.get(command.name);
        return (!structure.noDestination() || command.destinationLine == null) &&
                (!structure.noArgument() || command.argument == null || command.argument.isEmpty());
    }

}
