package chain4j.common;

import java.util.concurrent.ExecutorService;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.ILink;
import chain4j.builder.ChainBuilder;
import chain4j.internal.AbstractChain;
import chain4j.internal.Linker;
import chain4j.internal.Linker.IThreaded;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Utility methods for {@link IChain}s
 *
 * @author wassj
 *
 */
public enum Chains {
	/*singleton-enum*/;

	public static IChain create(final ICommand command) {
		return builder(command).build();
	}


	public static ChainBuilder builder(final ICommand command) {
		return ChainBuilder.create(command);
	}


	public static IChain makeThreaded(final IChain chain, final ExecutorService executor) {
		return new ChainThreadingDecorator(chain, executor);
	}


	public static IChain makeUndoable(IChain chain) {
		return new UndoChainDecorator(chain);
	}


	public static void invokeQuietly(final IChain chain)
		throws RuntimeException {

		try {
			chain.invoke();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static void invokeQuietly(final IChain chain, Object dto)
		throws RuntimeException {

		try {
			chain.invoke(dto);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * 
	 *
	 *
	 * @author wassj
	 *
	 */
	private static class UndoChainDecorator
		implements IChain {

		private final IChain chain;


		public UndoChainDecorator(final IChain chain) {
			this.chain = chain;
		}


		public void invoke(Object dto)
			throws Exception {

			MoreExecutors.sameThreadExecutor().submit(Linker.undo(chain.head(), dto)).get();
		}


		public void invoke()
			throws Exception {

			this.invoke(null);
		}


		public ILink head() {
			return chain.head();
		}
	}


	/**
	 * Decorate an {@link IChain} with an {@link ExecutorService} to provide threading capability
	 *
	 * @author wassj
	 *
	 */
	private static class ChainThreadingDecorator
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
}
