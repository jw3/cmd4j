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

	private ListeningExecutorService executor;
	private Object dto;


	protected AbstractChain(final Link head) {
		this.head = head;
	}


	public Link head() {
		return head;
	}


	public boolean isEmpty() {
		return head != null;
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
			executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
		}
		return executor;
	}


	/**
	 * set the executor for the chain
	 * @param executor
	 * @return
	 */
	public IChain executor(final ExecutorService executor) {
		this.executor = MoreExecutors.listeningDecorator(executor);
		return this;
	}
}
