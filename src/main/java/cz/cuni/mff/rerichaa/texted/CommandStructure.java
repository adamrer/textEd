package cz.cuni.mff.rerichaa.texted;

/**
 * Represents a command structure. Used for checking if command doesn't have something extra.
 * @param noDestination If command shouldn't have a destination line.
 * @param noArgument If command shouldn't have an argument.
 */
public record CommandStructure(boolean noDestination, boolean noArgument){

}

