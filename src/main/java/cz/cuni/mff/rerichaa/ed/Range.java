package cz.cuni.mff.rerichaa.ed;

/**
 * Class representing a range of line indices for TextEd command. State determines if a default range should be
 * used for the command.
 */
public class Range {
    public RangeState state;
    public int from;
    public int to;

    /**
     * Creates a range with specified values and sets the state to SETRANGE.
     * @param from Index where the range begins.
     * @param to Index where the range ends.
     */
    public Range(int from, int to){
        this.from = from;
        this.to = to;
        this.state = RangeState.RANGESET;
    }

    /**
     * Creates range in default state. Range should be set as the default.
     */
    public Range(){
        this.state = RangeState.DEFAULT;
    }
    public String toString(){
        return "(" + from + ", " + to + ") " + state;
    }
}
