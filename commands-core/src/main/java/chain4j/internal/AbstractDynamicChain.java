package chain4j.internal;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.ICommand2;
import chain4j.ILink;
import chain4j.ILink2;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Composition of multiple {@link ICommand}s into a single context which allows mutable flows
 * 
 * @author wassj
 *
 */
abstract public class AbstractDynamicChain
	extends AbstractChain
	implements IChain, FutureCallback<ILink> {

	private ILink next;
	private Object dto;


	protected AbstractDynamicChain() {
		super(new InitializerLink(), false);
	}


	protected AbstractDynamicChain(final boolean unthreaded) {
		super(new InitializerLink(), unthreaded);
	}


	protected void next(final ILink next) {
		this.next = next;
	}


	public void onSuccess(final ILink result) {
		final ILink next = result != null ? result : this.next;
		if (next != null) {
			this.executeLink(next);
		}
	}


	public void onFailure(Throwable t) {
		t.printStackTrace();
	}


	/**
	 * execute this chain
	 */
	final public void exec() {
		this.executeLink(this.head());
	}


	private void executeLink(final ILink link) {
		final ICommand command = link.iterator().next();
		if (command instanceof ICommand2) {
			link.dto(dto);
		}
		Futures.addCallback(this.executorOf(link).submit(link), this);
		// caller thread is done at this point....
	}


	private ListeningExecutorService executorOf(final ILink link) {
		if (link instanceof ILink2) {
			return ((ILink2)link).executor() != null ? ((ILink2)link).executor() : this.executor();
		}
		return this.executor();
	}


	/**
	 * Utility {@link Link} that will handle internal initialization
	 * 
	 * @author wassj
	 *
	 */
	private static class InitializerLink
		extends Link {

		public InitializerLink() {
			super(new ICommand() {
				public void invoke() {
					System.out.println("fsm init");
				}
			});
		}
	}
}
