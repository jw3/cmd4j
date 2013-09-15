package chain4j;

/**
 * A block of execution in the spirit of the GOF Command Pattern.
 *  command: 
 * 
 * Multiple Commands can be linked together using {@link ILink} objects to form a {@link IChain}.
 * Chains are then executed resulting in the commands being executed in the sequence they were built.
 *
 * This is a base class that serves as a tagging interface only.
 *
 * @author wassj
 *
 */
public interface ICommand {
}
