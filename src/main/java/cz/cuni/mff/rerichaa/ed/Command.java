package cz.cuni.mff.rerichaa.ed;

public class Command {

    public char name;
    public String argument;
    public Range range;
    public Integer destinationLine;

    public Command(String sCommand, int currLine, int lastLine){
        enum State {
            RANGELOW,
            RANGEHIGH,
            PLUS,
            MINUS,
            DESTINATION,
            ARGUMENT
        }
        name = ' ';
        char[] chars = sCommand.toCharArray();

        State state = State.RANGELOW;

        StringBuilder sb = new StringBuilder();
        boolean fromHigh = false;

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
                        }
                        state = State.RANGEHIGH;
                        sb = new StringBuilder();
                    }else if (ch == '.'){
                        sb.append(Integer.toString(currLine));
                    } else if (ch == '$'){
                        sb.append(Integer.toString(lastLine));
                    } else if (ch == '+' ){
                        state = State.PLUS;
                    }
                    else if (ch == '-'){
                        state = State.MINUS;
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
                case PLUS:
                    if (Character.isDigit(ch)){
                        sb.append(ch);
                    }
                    else if (Character.isLetter(ch)){
                        int rangeHigher = currLine +1;
                        if (!sb.isEmpty()){
                            rangeHigher += Integer.parseInt(sb.toString()) -1;
                            sb = new StringBuilder();
                        }
                        if (fromHigh)
                            range = new Range(rangeLower,rangeHigher);
                        else{
                            rangeLower = rangeHigher;
                            range = new Range(rangeHigher, rangeHigher);
                        }

                        name = ch;
                        state = State.DESTINATION;
                    }
                    else if (ch == ','){
                        int rangeHigher = currLine +1;

                        if (!sb.isEmpty()){
                            rangeHigher += Integer.parseInt(sb.toString()) -1;
                            sb = new StringBuilder();
                        }
                        if (fromHigh)
                            range = new Range(rangeLower,rangeHigher);
                        else{
                            rangeLower = rangeHigher;
                            range = new Range(rangeHigher, rangeHigher);
                        }

                        state = State.RANGEHIGH;
                    }
                    else{
                        System.out.println("!");
                    }

                    break;
                case MINUS:
                    if (Character.isDigit(ch)){
                        sb.append(ch);
                    }
                    else if (Character.isLetter(ch)){
                        int rangeHigher = currLine-1;
                        if (!sb.isEmpty()){
                            rangeHigher -= Integer.parseInt(sb.toString()) -1;

                            sb = new StringBuilder();

                        }
                        if (fromHigh)
                            range = new Range(rangeLower,rangeHigher);
                        else{
                            rangeLower = rangeHigher;
                            range = new Range(rangeHigher, rangeHigher);
                        }

                        name = ch;
                        state = State.DESTINATION;
                    }
                    else if (ch == ','){
                        int rangeHigher = currLine -1;

                        if (!sb.isEmpty()){
                            rangeHigher -= Integer.parseInt(sb.toString()) -1;
                            sb = new StringBuilder();
                        }
                        if (fromHigh)
                            range = new Range(rangeLower,rangeHigher);
                        else{
                            rangeLower = rangeHigher;
                            range = new Range(rangeHigher, rangeHigher);
                        }

                        state = State.RANGEHIGH;
                    }
                    else{
                        System.out.println("!");
                    }
                    break;
                case RANGEHIGH:
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
                    } else if (ch == '+'){
                        fromHigh = true;
                        state = State.PLUS;

                    } else if (ch == '-'){
                        fromHigh = true;
                        state = State.MINUS;
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
        if (state == State.RANGELOW && !sb.isEmpty())
            range = new Range(Integer.parseInt(sb.toString()), Integer.parseInt(sb.toString()));
        else if (state == State.MINUS || state == State.PLUS){

            if (!sb.isEmpty()){
                int num = Integer.parseInt(sb.toString());
                if (state == State.PLUS)
                    range = new Range(currLine + num, currLine + num);
                else
                    range = new Range(currLine - num, currLine - num);
            }
            else
                if (state == State.PLUS)
                    range = new Range(currLine+1, currLine+1);
                else
                    range = new Range(currLine-1, currLine-1);
        }

        else if (state == State.ARGUMENT && !sb.isEmpty())
            argument = sb.toString();

        else if (state == State.DESTINATION && !sb.isEmpty())
            destinationLine = Integer.parseInt(sb.toString());

    }

    public String toString(){
        return String.format("%s %s %d %s", range, name, destinationLine, argument);
    }


}
