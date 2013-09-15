package cmd4j.internal;

import cmd4j.IChain;
import cmd4j.ILink;

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
