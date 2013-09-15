package chain4j;

import chain4j.internal.Link;

/**
<pre>
 * A {@link IChain} is normally linked with a predetermined path of execution.  Using an {@link IBranch} 
 * allows the execution to pursue alternate paths based on logic that is executed at runtime.
 * 
 * This can be thought of as a 'dynamic' {@link ILink}, as it allows the next link to be identified at runtime.
 *
 * @author wassj
</pre>
 */
abstract public class IBranch
	extends Link
	implements ILink {

	/**
	 * returns the next link to execute
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	abstract public ILink invoke(final Object dto)
		throws Exception;


	protected IBranch() {
		super(null, null);
	}


	final public ILink call()
		throws Exception {

		return this.invoke(dto());
	}
}
