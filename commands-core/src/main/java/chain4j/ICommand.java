package chain4j;

/**
 * A block of execution following the GOF Command Pattern.  
 * Multiple commands can be linked together using {@link ILink} objects to form an {@link IChain}
 *
 * @author wassj
 *
 */
public interface ICommand {

	void invoke()
		throws Exception;
}
