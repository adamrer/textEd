package cz.cuni.mff.rerichaa.texted;

/**
 * Class representing TextEd command.
 */
public class Command {
    public char name; // letter, '?', ' '
    public String argument; // for specifying a file
    public Range range; // where the command will have an effect
    public Integer destinationLine; // additional index for copying or moving lines
    public String regex; // regular expression for substitute command
    public String replacement; // replacement text that will replace text that matched the regex
    public String suffixes; // additional suffixes

    public ErrorType error = null; // error that occurred while parsing the command

    /**
     * State automaton that parses command from string to Command instance. Current line and last line are for
     * translating ',', '$' and '.' to indices.
     * @param sCommand Command in string that will be parsed.
     * @param currLine Current line of the TextEd.
     * @param lastLine Last line of the TextEd.
     */
    public Command(String sCommand, int currLine, int lastLine){
        // States of the automaton
        enum State {
            /**
             * Loading lower index of range
             */
            RANGELOW,
            /**
             * Loading higher index of range
             */
            RANGEHIGH,
            /**
             * Translating relative addressing
             */
            PLUS,
            /**
             * Loading destination line
             */
            DESTINATION,
            /**
             * Loading argument
             */
            ARGUMENT,
            /**
             * Loading regular expression
             */
            REGEX,
            /**
             * Loading replacement string
             */
            REPLACEMENT,
            /**
             * Loading suffixes
             */
            SUFFIXES
        }
        name = ' ';
        char[] chars = sCommand.toCharArray();

        State state = State.RANGELOW;

        StringBuilder sb = new StringBuilder(); // for loading values
        boolean fromHigh = false; // going from RANGEHIGH to PLUS
        Integer plusValue = null; // add to this number in PLUS if set
        boolean firstSlashRead = false; // beginning of regex read
        boolean escape = false; // escaping '/' and '\' in the substitute command

        int rangeLower = -1;
        for (char ch : chars){
            switch (state){
                case RANGELOW:
                    if (Character.isDigit(ch)){
                        sb.append(ch);
                    } else if (ch == ',') {
                        if (!sb.isEmpty()){
                            rangeLower = Integer.parseInt(sb.toString());
                            range = new Range(rangeLower, rangeLower);
                        }
                        else {
                            if (lastLine > 0)
                                range = new Range(1 , lastLine);
                            else {
                                createInvalidCommand(ErrorType.ADDRESS);
                                return;
                            }
                        }
                        state = State.RANGEHIGH;
                        sb = new StringBuilder();
                    }else if (ch == '.'){
                        sb.append(Integer.toString(currLine));
                    } else if (ch == '$'){
                        sb.append(Integer.toString(lastLine));
                    } else if (ch == '+' || ch == '-'){// relative addressing
                        state = State.PLUS;
                        if (!sb.isEmpty()){
                            if (ch == '+')
                                plusValue = Integer.parseInt(sb.toString());
                            else
                                plusValue = Integer.parseInt('-'+sb.toString());
                            sb = new StringBuilder();
                        }
                        else if (ch == '-')
                            plusValue = -1;

                    } else if (Character.isLetter(ch)){
                        // only one number in range
                        if (!sb.isEmpty()){
                            rangeLower = Integer.parseInt(sb.toString());
                            range = new Range(rangeLower, rangeLower);
                        }
                        else{ // no number given
                            range = new Range();
                        }
                        sb = new StringBuilder();
                        name = ch;
                        if (name == 's')
                            state = State.REGEX;
                        else
                            state = State.DESTINATION;
                    }
                    else{
                        createInvalidCommand(ErrorType.UNKNOWN_COMMAND);
                    }

                    break;
                case RANGEHIGH:
                    if (Character.isDigit(ch)){
                        sb.append(ch);
                    } else if (Character.isLetter(ch)){

                        if (!sb.isEmpty()){
                            range = new Range(rangeLower, Integer.parseInt(sb.toString()));
                            sb = new StringBuilder();

                        }
                        if (ch == 's')
                            state = State.REGEX;
                        else
                            state = State.DESTINATION;
                        name = ch;
                    } else if (ch == '.'){
                        sb.append(Integer.toString(currLine));
                    } else if (ch == '$'){
                        sb.append(Integer.toString(lastLine));
                    } else if (ch == '+' || ch == '-'){
                        state = State.PLUS;
                        if (!sb.isEmpty()){
                            if (ch == '+')
                                plusValue = Integer.parseInt(sb.toString());
                            else
                                plusValue = Integer.parseInt('-'+sb.toString());
                            sb = new StringBuilder();
                        }
                        else if (ch == '-')
                            plusValue = -1;
                        fromHigh = true;

                    }
                    else{
                        createInvalidCommand(ErrorType.UNKNOWN_COMMAND);
                    }
                    break;
                case PLUS:
                    if (Character.isDigit(ch)){
                        sb.append(ch);
                    }
                    else if (Character.isLetter(ch)){
                        int rangeHigher = currLine +1;

                        if (plusValue != null)
                            if (plusValue.equals(-1))
                                rangeHigher = currLine -1;
                            else
                                rangeHigher = plusValue;

                        if (!sb.isEmpty()){
                            int num = Integer.parseInt(sb.toString());
                            if (plusValue == null)
                                if (num > 0)
                                    rangeHigher += num -1;
                                else
                                    rangeHigher += (num +1)*(-1);
                            else
                                if (plusValue > 0)
                                    rangeHigher += num;
                                else if (!plusValue.equals(-1)){
                                    rangeHigher = -(plusValue + num);
                                }
                            sb = new StringBuilder();
                        }

                        if (fromHigh)
                            range = new Range(rangeLower,rangeHigher);
                        else{
                            if (rangeHigher < 0)
                                rangeLower = -rangeHigher -1;
                            else rangeLower = rangeHigher;
                            range = new Range(rangeLower, rangeLower);
                        }

                        plusValue = null;
                        name = ch;
                        if (name == 's')
                            state = State.REGEX;
                        else
                            state = State.DESTINATION;
                    }
                    else if (ch == ','){

                        int rangeHigher = currLine +1;


                        if (plusValue != null)
                            if (plusValue.equals(-1))
                                rangeHigher = currLine -1;
                            else
                                rangeHigher = plusValue;

                        if (!sb.isEmpty()){
                            int num = Integer.parseInt(sb.toString());
                            if (plusValue == null)
                                if (num > 0)
                                    rangeHigher += num -1;
                                else
                                    rangeHigher += (num +1)*(-1);
                            else
                                if (plusValue > 0)
                                    rangeHigher += num;
                                else if (!plusValue.equals(-1))
                                    rangeHigher = -(plusValue + num);
                                else
                                    rangeHigher = currLine - num;
                            rangeLower = rangeHigher;
                            range = new Range(rangeLower, rangeLower);

                        }
                        else {
                            if (fromHigh)
                                range = new Range(rangeLower, rangeHigher);
                            else {
                                if (rangeHigher < 0)
                                    rangeLower = -rangeHigher - 1;
                                else rangeLower = rangeHigher;
                                range = new Range(rangeLower, rangeLower);
                            }

                        }
                        plusValue = null;
                        sb = new StringBuilder();
                        state = State.RANGEHIGH;
                    }
                    else{
                        createInvalidCommand(ErrorType.UNKNOWN_COMMAND);
                        return;
                    }

                    break;

                case DESTINATION:
                    if (Character.isDigit(ch)){
                        sb.append(ch);
                    } else{
                        if (!sb.isEmpty()){
                            destinationLine = Integer.parseInt(sb.toString());
                        }
                        else{
                            destinationLine = null;
                        }
                        sb = new StringBuilder();

                        state = State.ARGUMENT;

                    }
                    break;
                case REGEX:
                    if (ch == '/' && !firstSlashRead){
                        firstSlashRead = true;
                    } else if (ch != '/' && !firstSlashRead){
                        createInvalidCommand(ErrorType.DELIMITER);
                        return;
                    }
                    else if (ch == '\\') {
                        if (escape) {
                            sb.append("\\\\");
                            escape = false;
                        }
                        else
                            escape = true;
                    }
                    else if (ch != '/' || escape ){
                        if (escape && ch!= '/')
                            sb.append('\\').append(ch);
                        else
                            sb.append(ch);
                        escape = false;

                    }
                    else {// found second '/'
                        regex = sb.toString();
                        state = State.REPLACEMENT;
                        sb = new StringBuilder();

                    }
                    break;
                case REPLACEMENT:
                    if (ch != '/')
                        sb.append(ch);

                    else {
                        replacement = sb.toString();
                        state = State.SUFFIXES;
                        sb = new StringBuilder();
                    }
                    break;
                case SUFFIXES:
                    if (ch == 'g' || ch == 'I' || ch == 'i' || ch == 'n' || Character.isDigit(ch))
                        sb.append(ch);
                    else {
                        createInvalidCommand(ErrorType.SUFFIX);
                        return;
                    }
                    break;
                case ARGUMENT:
                    if (!Character.isWhitespace(ch)){
                        sb.append(ch);
                    }
                    break;

            }

        }

        if (name == '?')
            return;
        if (state == State.RANGELOW && !sb.isEmpty())
            range = new Range(Integer.parseInt(sb.toString()), Integer.parseInt(sb.toString()));
        else if (state == State.PLUS){
            if (!sb.isEmpty()){
                int num = Integer.parseInt(sb.toString());
                if (plusValue != null)
                    if (plusValue > 0)
                        range = new Range(plusValue + num, plusValue + num);
                    else if (plusValue.equals(-1))
                        range = new Range(currLine + num*plusValue, currLine + num*plusValue);
                    else
                        range = new Range(-(plusValue + num), -(plusValue + num));

                else
                    range = new Range(currLine + num, currLine + num);

            } else {
                if (plusValue != null && plusValue != -1)
                    range = new Range(plusValue + 1, plusValue + 1);
                else if (plusValue == null)
                    range = new Range(currLine +1, currLine+1);
                else
                    range = new Range(currLine - 1, currLine - 1);
            }
        }
        else if (state == State.REGEX){
            createInvalidCommand(ErrorType.DELIMITER);
        }
        else if (state == State.REPLACEMENT && replacement == null){
            replacement = sb.toString();
            suffixes = "";
        }
        else if (state == State.SUFFIXES){
            suffixes = sb.toString();
        }
        else if (state == State.ARGUMENT && !sb.isEmpty())
            argument = sb.toString();

        else if (state == State.DESTINATION && !sb.isEmpty())
            destinationLine = Integer.parseInt(sb.toString());

        //check boundaries
        if (range != null && range.state == RangeState.RANGE_SET &&
                (range.from > range.to ||
                        range.from < 0 ||
                        range.to > lastLine))
            createInvalidCommand(ErrorType.ADDRESS);

    }

    /**
     * Creates invalid command. Called when the command is invalid.
     * @param error Type of the error that made it invalid.
     */
    private void createInvalidCommand(ErrorType error){
        range = null;
        name = '?';

        this.error = error;
    }
    public String toString(){
        return String.format("%s %s %d %s %s %s %s", range, name, destinationLine, argument, regex, replacement, suffixes);
    }


}
