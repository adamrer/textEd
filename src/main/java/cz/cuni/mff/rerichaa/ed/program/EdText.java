package cz.cuni.mff.rerichaa.ed.program;


import cz.cuni.mff.rerichaa.ed.Command;
import cz.cuni.mff.rerichaa.ed.CommandStructure;
import cz.cuni.mff.rerichaa.ed.Range;
import cz.cuni.mff.rerichaa.ed.RangeState;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class EdText {

    static List<String> buffer = new ArrayList<>();
    static String defaultFile;
    static int currLine;
    static boolean showPrompt = false;

    static Hashtable<Character, CommandStructure> knownCommands = new Hashtable<>(){
        {
            //prompt
            put('P', new CommandStructure(false,false, false, false));
            //print
            put('p', new CommandStructure(true,false, false, false));
            //read
            put('r', new CommandStructure(false,false, false, true));
            //change current line
            put(' ', new CommandStructure(true,true, false, false));
            //change lines
            put('c', new CommandStructure(true,false, false, false));
            //delete lines
            put('d', new CommandStructure(true, false, false, false));
            //number lines
            put('n', new CommandStructure(true, false, false, false));
            //write buffer to file
            put('w', new CommandStructure(true, false, false, false));

        }
    };

    private static void initialize(){
        buffer = new ArrayList<>();
        buffer.add("");
        currLine = 0;
        defaultFile = null;

    }
    public static void main(String[] args){
        initialize();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        try(BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) )){
            if (showPrompt) System.out.print("* ");
            String input = reader.readLine();

            // main loop
            while(!input.equals("q")){
                Command command = new Command(input, currLine, buffer.size()-1);

                System.out.println(command);

                //if (checkStructure(command))
                execute(command, reader);
                //else System.out.println("? Invalid command");

                if (showPrompt) System.out.print("* ");
                input = reader.readLine();

            }
        }
        catch (java.io.IOException e){
            System.out.println("Error reading input");
        }


    }
    public static void execute(Command command, BufferedReader inputReader){
        switch(command.name){
            case 'r':
                readFile(command.argument, command.range);
                break;
            case 'p':
                if (command.range.state == RangeState.FULLRANGE && buffer.size()>1) command.range = new Range(1, buffer.size()-1);
                else if (command.range.state == RangeState.DEFAULT) command.range = new Range(currLine, currLine);

                printBuffer(command.range);
                break;
            case ' ':
                if (command.range != null){

                    int lineNumber = command.range.from;
                    if (lineNumber < buffer.size()) currLine = lineNumber;
                    else System.out.println("! Invalid address");
                }
                else
                    System.out.println("! Invalid command");
                break;
            case 'P':
                if (command.range != null){
                    System.out.println("!");
                    return;
                }
                showPrompt = !showPrompt;
                break;
            case 'c':
                changeLines(command.range, inputReader);
                break;
            case 'a':
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(buffer.size()-1, buffer.size()-1);
                else
                    System.out.println("!");

                if (command.range == null) command.range = new Range(buffer.size(), buffer.size());
                appendLines(command.range.from, inputReader);
                break;
            case 'd':
                if (command.range.state == RangeState.DEFAULT)
                    command.range = new Range(currLine, currLine);
                else if (command.range.state == RangeState.FULLRANGE && buffer.size()>1)
                    command.range = new Range(1, buffer.size()-1);

                if (command.range.to >= command.range.from) {
                    buffer.subList(command.range.from, command.range.to + 1).clear();
                }
                break;
            case 'n':
                if (command.range.state == RangeState.DEFAULT && buffer.size() > 1)
                    command.range = new Range(1, buffer.size()-1);
                else {
                    System.out.println("!");
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
                    System.out.println("! Default file is not set");


                break;
        }

    }
    private static void setRange(Command command, Range defaultRange, Range fullRange){
        switch (command.range.state){
            case DEFAULT -> command.range = fullRange;
            case FULLRANGE -> command.range = defaultRange;

        }
    }
    private static void appendLines(int from, BufferedReader inputReader){
        int index = from;
        try{
            String line = inputReader.readLine();
            while(!line.equals(".")){
                buffer.add(line);
                index++;
                line = inputReader.readLine();
            }
        }catch(java.io.IOException e){
            System.out.println("! Error reading input");
        }

        currLine = index - 1;
    }
    private static void readFile(String filePath, Range range){
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){


            String line = reader.readLine();
            int charSum = 0;

            if (range == null){
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
            System.out.println("? Invalid argument");
        }
    }
    private static void changeLines(Range range, BufferedReader inputReader){
        if (range.to >= range.from) {
            buffer.subList(range.from, range.to + 1).clear();
        }
        int index = range.from;
        try{
            String line = inputReader.readLine();
            while(!line.equals(".")){
                buffer.add(index, line);
                index++;
                line = inputReader.readLine();
            }
        }catch(java.io.IOException e){
            System.out.println("! Error reading input");
        }

        currLine = index - 1;

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
            if (range == null){
                for (String line : buffer) {
                    writer.write(line + "\n");
                }
            }
            else{
                for (int i = range.from; i <= range.to; i++) {
                    writer.write(buffer.get(i) + "\n");
                }
            }

        }catch(java.io.IOException e){
            System.out.println("Error writing to file");
        }
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
