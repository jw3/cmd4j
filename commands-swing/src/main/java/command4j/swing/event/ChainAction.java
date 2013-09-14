package command4j.swing.event;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import chain4j.IChain;
import chain4j.common.Chains;

/**
 *
 *
 * @author wassj
 *
 */
public class ChainAction
	extends AbstractAction {

	private IChain chain;


	public static ChainAction create() {
		return new ChainAction();
	}


	public static ChainAction create(IChain chain) {
		final ChainAction chainAction = new ChainAction();
		chainAction.setChain(chain);
		return chainAction;
	}


	protected ChainAction() {
	}


	public IChain getChain() {
		return chain;
	}


	public void setChain(IChain chain) {
		this.chain = chain;
	}


	final public void actionPerformed(ActionEvent e) {
		final IChain chain = this.getChain();
		if (chain != null) {
			Chains.invokeQuietly(chain);
		}
	}


	public ChainAction setValue(String key, String value) {
		this.putValue(key, value);
		return this;
	}
}
