package cz.cuni.mff.rerichaa.ed;

public class Command {

    public char name;
    public String argument;
    public Range range;
    public Integer destinationLine;

    public Command(String sCommand, Integer currLine, Integer lastLine){
        enum State {
            RANGE1,
            RANGE2,
            ALLRANGE,
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
                        } else {
                            range = new Range(RangeState.FULLRANGE);
                        }
                        state = State.RANGE2;
                        sb = new StringBuilder();
                    } else if (Character.isLetter(ch)){
                        // only one number in range
                        if (!sb.isEmpty()){
                            rangeLower = Integer.parseInt(sb.toString());
                            range = new Range(rangeLower, rangeLower);
                        }
                        else{ // no number given
                            if (currLine > 0){
                                range = new Range(RangeState.DEFAULT);
                            }
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
                    }
                    break;
                case ALLRANGE:
                    if (Character.isLetter(ch)) name = ch;
                    state = State.DESTINATION;
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
        if (state == State.RANGE1) range = new Range(Integer.parseInt(sb.toString()), Integer.parseInt(sb.toString()));
        else if (state == State.ARGUMENT) argument = sb.toString();

    }
    public String toString(){
        return String.format("%s %s %d %s", range, name, destinationLine, argument);
    }


}
