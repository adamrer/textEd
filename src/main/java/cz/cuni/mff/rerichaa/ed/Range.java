package cz.cuni.mff.rerichaa.ed;


public class Range {
    public RangeState state;
    public int from;
    public int to;
    public Range(int from, int to){
        this.from = from;
        this.to = to;
        this.state = RangeState.SETRANGE;
    }
    public Range(RangeState state){
        if (state != RangeState.SETRANGE) {
            this.state = state;
        }
    }
    public String toString(){
        return "(" + from + ", " + to + ") " + state;
    }
}
// comment
