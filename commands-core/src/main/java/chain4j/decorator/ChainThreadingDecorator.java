package chain4j.decorator;

import java.util.concurrent.ExecutorService;

import chain4j.IChain;
import chain4j.common.IThreaded;
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
	private ListeningExecutorService executor;


	/**
	 * unthreaded ctor
	 * @param chain
	 */
	public ChainThreadingDecorator(final IChain chain, final ExecutorService executor) {
		super(chain.head());
		this.chain = chain;
		this.executor = MoreExecutors.listeningDecorator(executor);
	}


	public ListeningExecutorService executor() {
		if (executor == null) {
			executor = MoreExecutors.sameThreadExecutor();
		}
		return executor;
	}


	public void invoke()
		throws Exception {

		this.invoke(null);
	}


	public void invoke(Object dto)
		throws Exception {

		this.executor.submit(Linker.create(chain.head(), dto)).get();
	}
}
