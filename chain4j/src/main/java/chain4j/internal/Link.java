package chain4j.internal;

import java.util.concurrent.Callable;

import chain4j.IChainable;

import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Provides the context in which an {@link IChainable} executes.  Can combine together with other Links to form a chain.
 *
 * @author wassj
 *
 */
public class Link
	implements Callable<Link> {

	private final ListeningExecutorService executor;
	private final IChainable chainable;
	private final Link next;

	private boolean failsafe;


	public Link(IChainable chainable) {
		this(chainable, null, null);
	}


	public Link(IChainable chainable, Link next) {
		this(chainable, next, null);
	}


	public Link(IChainable chainable, ListeningExecutorService executor) {
		this(chainable, null, executor);
	}


	public Link(IChainable chainable, Link next, ListeningExecutorService executor) {
		this.chainable = chainable;
		this.next = next;
		this.executor = executor;
	}


	public ListeningExecutorService getExecutor() {
		return executor;
	}


	public boolean isFailsafe() {
		return failsafe;
	}


	public Link setFailsafe(boolean failsafe) {
		this.failsafe = failsafe;
		return this;
	}


	/**
	 * Execute the {@link IChainable} and return the next link
	 */
	public Link call()
		throws Exception {

		try {
			chainable.invoke();
			return next;
		}
		catch (Exception e) {
			if (failsafe) {
				return next;
			}
			throw e;
		}
	}
}
