package chain4j.internal;

import chain4j.IChain;
import chain4j.ILink;

/**
 * Base implementation of an {@link IChain}
 * 
 * @author wassj
 *
 */
abstract public class AbstractChain
	implements IChain {

	private final ILink head;


	protected AbstractChain(final ILink head) {
		this.head = head;
	}


	public ILink head() {
		return head;
	}


	public boolean isEmpty() {
		return head != null;
	}
}
