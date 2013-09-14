package chain4j.fsm;

import chain4j.ILink;
import chain4j.internal.Link;

/**
 *
 *
 * @author wassj
 *
 */
abstract public class State
	extends Link {

	public State() {
		super(null);
	}


	final public ILink call()
		throws Exception {

		return run(this.dto());
	}


	/**
	 * provide the code to be executed by this state
	 */
	abstract public ILink run(Object dto)
		throws Exception;
}
