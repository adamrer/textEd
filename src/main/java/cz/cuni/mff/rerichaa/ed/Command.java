package cz.cuni.mff.rerichaa.ed;

import javax.swing.*;

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
            DESTINATION,
            ARGUMENT
        }
        name = ' ';
        char[] chars = sCommand.toCharArray();

        State state = State.RANGELOW;

        StringBuilder sb = new StringBuilder();
        boolean fromHigh = false; // going from RANGEHIGH to PLUS
        Integer plusValue = null; // add to this number in PLUS if set

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

                    } else if (Character.isLetter(ch)){
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
                        System.out.println("!"); //TODO: vytvořit neplatný command
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

            }
            else
                if (plusValue != null && plusValue != -1)
                    range = new Range(plusValue + 1, plusValue + 1);
                else if (plusValue == null)
                    range = new Range(currLine +1, currLine+1);
                else
                    range = new Range(currLine - 1, currLine - 1);

        }

        else if (state == State.ARGUMENT && !sb.isEmpty())
            argument = sb.toString();

        else if (state == State.DESTINATION && !sb.isEmpty())
            destinationLine = Integer.parseInt(sb.toString());

        //TODO: check boundaries

    }
    public String toString(){
        return String.format("%s %s %d %s", range, name, destinationLine, argument);
    }


}
