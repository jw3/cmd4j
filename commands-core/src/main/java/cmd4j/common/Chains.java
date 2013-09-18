package cmd4j.common;

import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.ILink;
import cmd4j.internal.AbstractChain;
import cmd4j.internal.Linker;
import cmd4j.internal.Linker.IThreaded;

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

	/**
	 * create a {@link IChain} that contains the given {@link ICommand}
	 * @param command
	 * @return {@link IChain}
	 */
	public static IChain create(final ICommand command) {
		return ChainBuilder.create(command).build();
	}


	/**
	 * specify that the {@link IChain} will run on the given {@link ExecutorService}
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static IChain makeThreaded(final IChain chain, final ExecutorService executor) {
		return new ChainThreadingDecorator(chain, executor);
	}


	/**
	 * add undo support to a {@link IChain}
	 * @param chain
	 * @return
	 */
	public static IChain makeUndoable(IChain chain) {
		return new UndoChainDecorator(chain);
	}


	/**
	 * enable visitor mode in a {@link IChain}
	 * @param chain
	 * @return
	 */
	public static IChain makeVisitable(IChain chain) {
		return new VisitorModeDecorator(chain);
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
	 * Decorator that provides undo capabiltiy for an {@link IChain}
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


		/**
		 * override the call to the Linker invoking the undo method
		 */
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


	/**
	 * Decorator that provides Visitor Pattern like behavior for an {@link IChain}
	 * 
	 * This is visitor-like in the sense that we lift the requirement for all Data Transfer Object 
	 * accepting {@link ICommand}s to have an assignable type to the dto that the Chain receives.
	 * 
	 * That allows you to create a chain of Commands, and pass a DTO to the chain, and have only
	 * the applicable Commands process that object. So similar to the Visitor Pattern we base calling
	 * on (1) The dynamic type of the element and (2) The dynamic type of the visitor.
	 *
	 * @author wassj
	 *
	 */
	private static class VisitorModeDecorator
		implements IChain {

		private final IChain chain;


		public VisitorModeDecorator(final IChain chain) {
			this.chain = chain;
		}


		/**
		 * override the call to the Linker invoking the undo method
		 */
		public void invoke(Object dto)
			throws Exception {

			MoreExecutors.sameThreadExecutor().submit(Linker.create(chain.head(), dto).visitable(true)).get();
		}


		public void invoke()
			throws Exception {

			this.invoke(null);
		}


		public ILink head() {
			return chain.head();
		}
	}
}
