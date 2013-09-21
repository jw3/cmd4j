package cmd4j.common;

import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.internal.ChainDecorator;

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
		return new ChainDecorator(chain, executor);
	}


	/**
	 * add undo support to a {@link IChain}
	 * @param chain
	 * @return
	 */
	public static IChain makeUndoable(IChain chain) {
		return new ChainDecorator(chain).undo();
	}


	/**
	 * enable visitor mode in a {@link IChain}
	 * @param chain
	 * @return
	 */
	public static IChain makeVisitable(IChain chain) {
		return new ChainDecorator(chain).visitable();
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
}
