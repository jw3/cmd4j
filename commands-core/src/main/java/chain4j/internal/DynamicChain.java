package chain4j.internal;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.ICommand2;
import chain4j.ILink;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * An {@link IChain} implementation that is linked dynamically to allow for State Machine like flow.
 * 
 * @author wassj
 *
 */
abstract public class DynamicChain
	extends AbstractChain
	implements IChain, FutureCallback<ILink> {

	private ILink next;
	private ListeningExecutorService executor;


	protected DynamicChain() {
		super(new InitializerLink());
	}


	protected ILink next() {
		return next;
	}


	protected void next(final ILink next) {
		this.next = next;
	}


	public void onSuccess(final ILink result) {
		final ILink next = result != null ? result : this.next();

		// clear out the member next so the end can be detected
		this.next(null);

		if (next != null) {
			this.executeLink(next, next.dto());
		}
	}


	public void onFailure(Throwable t) {
		t.printStackTrace();
	}


	/**
	 * execute this chain
	 */
	final public void invoke() {
		this.invoke(null);
	}


	/**
	 * execute this chain
	 */
	final public void invoke(final Object dto) {
		this.executeLink(this.head(), dto);
	}


	private void executeLink(final ILink link, final Object dto) {
		final ICommand command = link.iterator().next();
		if (command instanceof ICommand2) {
			link.dto(dto);
		}
		Futures.addCallback(executor().submit(link), this);
	}


	private ListeningExecutorService executor() {
		if (executor == null) {
			this.executor = MoreExecutors.sameThreadExecutor();
		}
		return executor;
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
