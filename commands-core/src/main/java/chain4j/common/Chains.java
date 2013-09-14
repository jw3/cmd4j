package chain4j.common;

import chain4j.IChain;
import chain4j.ILink;
import chain4j.internal.Linker;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * Utility methods for {@link IChain}s
 *
 * @author wassj
 *
 */
public enum Chains {
	/*singleton-enum*/;

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


	public static IChain undo(IChain chain) {
		return new UndoChainDecorator(chain);
	}


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
}
