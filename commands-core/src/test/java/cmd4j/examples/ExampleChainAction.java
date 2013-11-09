package cmd4j.examples;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import cmd4j.IChain;

/**
 *
 *
 * @author wassj
 *
 */
abstract public class ExampleChainAction
	extends AbstractAction {

	//private IChain chain;

	/*public static ChainAction create() {
		return new ChainAction() {
		};
	}


	public static ChainAction create(IChain chain) {
		final ChainAction chainAction = new ChainAction();
		chainAction.setChain(chain);
		return chainAction;
	}*/

	protected ExampleChainAction() {
	}


	abstract public IChain<Void> getChain();


	final public void actionPerformed(ActionEvent e) {
		final IChain<Void> chain = this.getChain();
		if (chain != null) {
			try {
				chain.invoke();
			}
			catch (Exception ex) {
				// NOTE Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}


	public ExampleChainAction setValue(String key, String value) {
		this.putValue(key, value);
		return this;
	}
}
