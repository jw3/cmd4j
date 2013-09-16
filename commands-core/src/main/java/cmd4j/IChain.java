package cmd4j;

/**
 * A handle on a set of {@link ILink} objects that will execute sequentially,
 * a la 'links in a chain'.  Provides an empty context in which those links execute.
 * Can be decorated to add to that context things such as concurrency and undo support. 
 * 
 * Implements {@link ICommand1} and {@link ICommand2} in order to:
 *  1) Allow {@link IChain} to be linked with other Chains just as an {@link ICommand} would.
 *  2) Provide dual means of invocation, {@link #invoke()} and {@link #invoke(Object)}.  The later
 *     of which will specify the Data Transfer Object for the Chain.
 *
 * @author wassj
 *
 */
public interface IChain
	extends ICommand1, ICommand2<Object> {

	/**
	 * the first {@link ILink} that will be called when this chain executes.
	 * @return {@link ILink} the first link
	 */
	ILink head();
}
