package chain4j.internal;

import java.util.concurrent.Callable;

import chain4j.IChainable;
import chain4j.IChainable2;

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
	private Object dto;


	public Link(final IChainable chainable) {
		this(chainable, null, null);
	}


	public Link(final IChainable chainable, final Link next) {
		this(chainable, next, null);
	}


	public Link(final IChainable chainable, final ListeningExecutorService executor) {
		this(chainable, null, executor);
	}


	public Link(final IChainable chainable, Link next, final ListeningExecutorService executor) {
		this.chainable = chainable;
		this.next = next;
		this.executor = executor;
	}


	public ListeningExecutorService executor() {
		return executor;
	}


	public boolean isFailsafe() {
		return failsafe;
	}


	public Link setFailsafe(final boolean failsafe) {
		this.failsafe = failsafe;
		return this;
	}


	public Link dto(final Object dto) {
		if (chainable instanceof IChainable2) {
			this.dto = dto;
		}
		return this;
	}


	IChainable chainable() {
		return chainable;
	}


	/**
	 * Execute the {@link IChainable} and return the next link
	 */
	public Link call()
		throws Exception {

		try {
			if (dto != null) {
				((IChainable2)chainable).invoke(dto);
			}
			else {
				chainable.invoke();
			}
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
