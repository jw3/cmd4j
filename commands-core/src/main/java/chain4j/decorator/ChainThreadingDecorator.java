package chain4j.decorator;

import java.util.concurrent.ExecutorService;

import chain4j.IChain;
import chain4j.IThreaded;
import chain4j.internal.AbstractChain;
import chain4j.internal.Linker;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Decorate an {@link IChain} with an {@link ExecutorService} to provide threading capability
 *
 * @author wassj
 *
 */
public class ChainThreadingDecorator
	extends AbstractChain
	implements IChain, IThreaded {

	private final IChain chain;
	private final boolean unthreaded;
	private ListeningExecutorService executor;


	/**
	 * unthreaded ctor
	 * @param chain
	 */
	public ChainThreadingDecorator(final IChain chain, final ExecutorService executor) {
		super(chain.head());
		this.chain = chain;
		this.unthreaded = executor == null;
		this.executor = !this.unthreaded ? MoreExecutors.listeningDecorator(executor) : null;
	}


	public ListeningExecutorService executor() {
		if (executor == null) {
			executor = MoreExecutors.sameThreadExecutor();
		}
		return executor;
	}


	//	public ChainThreadingDecorator executor(final ExecutorService executor) {
	//		this.executor = MoreExecutors.listeningDecorator(executor);
	//		return this;
	//	}

	public boolean isUnthreaded() {
		return unthreaded;
	}


	public void run() {
		Linker.begin(chain.head(), chain.dto(), this.executor());
	}


	public IChain dto(Object dto) {
		chain.dto(dto);
		return this;
	}
}
