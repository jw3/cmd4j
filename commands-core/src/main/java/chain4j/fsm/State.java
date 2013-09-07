package chain4j.fsm;

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


	final public Link call()
		throws Exception {

		return run(this.dto());
	}


	/**
	 * provide the code to be executed by this state
	 */
	abstract public State run(Object dto)
		throws Exception;
}
