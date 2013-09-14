package chain4j.fsm;

import chain4j.ILink;
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

	public void setStart(final ILink link) {
		this.next(link);
	}
}
