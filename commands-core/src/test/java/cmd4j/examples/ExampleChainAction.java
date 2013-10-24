package cmd4j.examples;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import cmd4j.Chains;
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


	abstract public IChain getChain();


	final public void actionPerformed(ActionEvent e) {
		final IChain chain = this.getChain();
		if (chain != null) {
			Chains.invokeQuietly(chain);
		}
	}


	public ExampleChainAction setValue(String key, String value) {
		this.putValue(key, value);
		return this;
	}
}
