package cz.cuni.mff.rerichaa.ed.program;


import cz.cuni.mff.rerichaa.ed.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class TextEd {

    private static List<String> buffer = new ArrayList<>();
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
            put(ErrorType.CURRENTFILE, "Current file not set");
        }
    };
    static Hashtable<Character, CommandStructure> knownCommands = new Hashtable<>(){
        {
            //prompt
            put('P', new CommandStructure(false,false, false, false));
            //print
            put('p', new CommandStructure( true,false, false, false));
            //read
            put('r', new CommandStructure( false,false, false, true));
            //change current line
            put(' ', new CommandStructure(true,true, false, false));
            //change lines
            put('c', new CommandStructure(true,false, false, false));
            //delete lines
            put('d', new CommandStructure(true, false, false, false));
            //number lines
            put('n', new CommandStructure(true, false, false, false));
            //write buffer to file
            put('w', new CommandStructure( true, false, false, false));

        }
    };

    private static void initialize(){
        buffer = new ArrayList<>();
        buffer.add("");
        currLine = 0;
        defaultFile = null;
        showPrompt = false;

    }
    public static void main(String[] args){
        initialize();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        try(BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) )){
            if (showPrompt) System.out.print("* ");
            String input = reader.readLine();

            // main loop
            while(!input.equals("Q")){
                if (!changesMade && input.equals("q"))
                    break;
                Command command = new Command(input, currLine, buffer.size()-1);

                //if (checkStructure(command))
                execute(command, reader);

                //else System.out.println("? Invalid command");

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
            case '!':
                printError(command.error);
                break;
            case 'q':
                printError(ErrorType.UNSAVED);
                break;
            case 'h':
                System.out.println("! " + lastError);
                break;
            case 'H':
                if (command.range.state != RangeState.DEFAULT){
                    printError(ErrorType.ADDRESS);
                    return;
                }
                showHelp = !showHelp;
                break;
            case 'P':
                if (command.range.state != RangeState.DEFAULT){
                    printError(ErrorType.ADDRESS);
                    return;
                }
                showPrompt = !showPrompt;
                break;
            case 'r':

                if (command.argument == null && defaultFile == null)
                    printError(ErrorType.CURRENTFILE);
                else if (command.argument != null)
                    readFile(command.argument, command.range);
                else
                    readFile(defaultFile, command.range);
                break;
            case 'e':
                if (changesMade){
                    printError(ErrorType.UNSAVED);
                }
                else{
                    buffer = new ArrayList<>();
                    buffer.add("");
                    if (command.range.from <= 0){
                        printError(ErrorType.ADDRESS);
                        break;
                    }
                    readFile(command.argument, command.range);
                }

                break;
            case 'p':
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                if (command.range.from <= 0){
                    printError(ErrorType.ADDRESS);
                    break;
                }
                printBuffer(command.range);
                break;
            case ' ':
                if (command.range.state == RangeState.SETRANGE){

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

            case 'c':
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                if (command.range.from <= 0){
                    printError(ErrorType.ADDRESS);
                    break;
                }
                deleteLines(command.range);
                appendLines(command.range.from, inputReader);
                break;
            case 'a':
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(buffer.size(), buffer.size());
                appendLines(command.range.from, inputReader);
                break;
            case 'd':
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                if (command.range.from <= 0){
                    printError(ErrorType.ADDRESS);
                    break;
                }
                deleteLines(command.range);

                break;
            case 'n':
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
            case 'w':
                if ((command.argument == null || command.argument.isEmpty()) && !defaultFile.isEmpty())
                    writeBuffer(defaultFile, command.range);
                else if (command.argument != null && !command.argument.isEmpty())
                    writeBuffer(command.argument, command.range);
                else
                    printError(ErrorType.CURRENTFILE);
                break;
            default:
                printError(ErrorType.UNKNOWNCOMMAND);
                break;


        }

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
            System.out.println("! " + errorMessages.get(error));
        else
            System.out.println("!");
    }
    private static void appendLines(int from, BufferedReader inputReader){
        int index = from;
        try{
            String line = inputReader.readLine();
            while(!line.equals(".")){
                buffer.add(index, line);
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
        return (!structure.hasRange() || command.range != null) &&
                (!structure.onlyOneLine() || command.range.from == command.range.to) &&
                (!structure.hasDestination() || command.destinationLine != null) &&
                (!structure.argumentRequired() || (command.argument != null && !command.argument.isEmpty()));
    }

}
