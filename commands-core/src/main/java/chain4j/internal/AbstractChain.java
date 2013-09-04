package chain4j.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chain4j.IChain;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Base implementation of an {@link IChain}
 * 
 * @author wassj
 *
 */
abstract public class AbstractChain
	implements IChain {

	private final Link head;
	private final boolean unthreaded;

	private ListeningExecutorService executor;
	private Object dto;


	protected AbstractChain(final Link head) {
		this(head, false);
	}


	protected AbstractChain(final Link head, final boolean unthreaded) {
		this.head = head;
		this.unthreaded = unthreaded;
	}


	public Link head() {
		return head;
	}


	public boolean isEmpty() {
		return head != null;
	}


	public boolean isUnthreaded() {
		return unthreaded;
	}


	protected Object dto() {
		return dto;
	}


	/**
	 * set the dto on the chain
	 * @param dto
	 * @return
	 */
	public IChain dto(final Object dto) {
		this.dto = dto;
		return this;
	}


	protected ListeningExecutorService executor() {
		if (executor == null) {
			executor = MoreExecutors.listeningDecorator(unthreaded ? MoreExecutors.sameThreadExecutor() : Executors.newSingleThreadExecutor());
		}
		return executor;
	}


	/**
	 * set the executor for the chain
	 * @param executor
	 * @return
	 */
	public IChain executor(final ExecutorService executor) {
		if (unthreaded) {
			throw new IllegalStateException("cannot set executor when chain is unthreaded");
		}
		this.executor = MoreExecutors.listeningDecorator(executor);
		return this;
	}
}
