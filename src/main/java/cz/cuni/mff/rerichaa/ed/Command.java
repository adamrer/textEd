package cz.cuni.mff.rerichaa.ed;

public class Command {

    public char name;
    public String argument;
    public Range range;
    public Integer destinationLine;

    public Command(String sCommand, int currLine, int lastLine){
        enum State {
            RANGE1,
            RANGE2,
            DESTINATION,
            ARGUMENT
        }
        name = ' ';
        char[] chars = sCommand.toCharArray();

        State state = State.RANGE1;

        StringBuilder sb = new StringBuilder();

        int rangeLower = -1;
        for (char ch : chars){
            switch (state){
                case RANGE1:
                    if (Character.isDigit(ch)){
                        sb.append(ch);
                    } else if (ch == ',') {
                        if (!sb.isEmpty()){
                            rangeLower = Integer.parseInt(sb.toString());
                        }
                        else {
                            if (lastLine > 0)
                                range = new Range(1 , lastLine);
                        }
                        state = State.RANGE2;
                        sb = new StringBuilder();
                    }else if (ch == '.'){
                        sb.append(Integer.toString(currLine));
                    } else if (ch == '$'){
                        sb.append(Integer.toString(lastLine));
                    } else if (ch == '+'){
                        int nextLine = currLine +1;
                        sb.append(Integer.toString(nextLine));
                    }
                    else if (Character.isLetter(ch)){
                        // only one number in range
                        if (!sb.isEmpty()){
                            rangeLower = Integer.parseInt(sb.toString());
                            range = new Range(rangeLower, rangeLower);
                        }
                        else{ // no number given
                            range = new Range(RangeState.DEFAULT);
                        }
                        sb = new StringBuilder();
                        name = ch;
                        state = State.DESTINATION;
                    }

                    break;
                case RANGE2:
                    if (Character.isDigit(ch)){
                        sb.append(ch);
                    } else if (Character.isLetter(ch)){

                        if (!sb.isEmpty()){
                            range = new Range(rangeLower, Integer.parseInt(sb.toString()));
                            sb = new StringBuilder();
                            state = State.DESTINATION;
                        }
                        name = ch;
                    } else if (ch == '.'){
                        sb.append(Integer.toString(currLine));
                    } else if (ch == '$'){
                        sb.append(Integer.toString(lastLine));
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
                case ARGUMENT:
                    if (!Character.isWhitespace(ch)){
                        sb.append(ch);
                    }
                    break;
            }

        }
        if (state == State.RANGE1 && !sb.isEmpty())
            range = new Range(Integer.parseInt(sb.toString()), Integer.parseInt(sb.toString()));

        else if (state == State.ARGUMENT && !sb.isEmpty())
            argument = sb.toString();

    }
    public String toString(){
        return String.format("%s %s %d %s", range, name, destinationLine, argument);
    }


}
