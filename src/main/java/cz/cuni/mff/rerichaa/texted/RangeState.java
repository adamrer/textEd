package cz.cuni.mff.rerichaa.texted;

/**
 * Represents the state of Range. If range state is set to DEFAULT, it should be set to default values. If
 * range state is set to RANGESET, range is set.
 */
public enum RangeState {
    /**
     * Range must be set in TextEd execute method
     */
    DEFAULT,
    /**
     * Range is set
     */
    RANGE_SET
}
