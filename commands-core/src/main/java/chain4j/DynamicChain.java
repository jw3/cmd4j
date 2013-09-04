package chain4j;

import chain4j.internal.AbstractDynamicChain;

/**
 * 
 * 
 * @author wassj
 *
 */
public class DynamicChain
	extends AbstractDynamicChain {

	protected DynamicChain() {
		super(false);
	}


	protected DynamicChain(final boolean unthreaded) {
		super(unthreaded);
	}

	//-------------------------------------------------------
	//
	// defines some states
	// set next(state) to traverse the states
	// end that chain by not setting a next state
	//
	//-------------------------------------------------------
}
