package chain4j.fsm;

import chain4j.internal.DynamicChain;

/**
 * 
 *
 *
 * @author wassj
 *
 */
public class StateMachine
	extends DynamicChain {

	public void setStart(State state) {
		this.next(state);
	}
}
