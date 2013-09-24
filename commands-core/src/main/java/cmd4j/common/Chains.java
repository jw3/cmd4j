package cmd4j.common;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.ILink;
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
	 * creates an empty {@link IChain} which can be used in any operation a normal chain would, but will not do anything
	 * @return {@link IChain} the empty chain
	 */
	public static IChain empty() {
		return new EmptyChain();
	}


	/**
	 * create a {@link IChain} that contains the given {@link ICommand}
	 * @param command
	 * @return {@link IChain}
	 */
	public static IChain create(final Collection<ICommand> commands) {
		return create(commands.toArray(new ICommand[0]));
	}


	/**
	 * create a {@link IChain} that contains the given {@link ICommand}
	 * @param command
	 * @return {@link IChain}
	 */
	public static IChain create(final ICommand... commands) {
		final ChainBuilder builder = ChainBuilder.create();
		for (ICommand command : commands) {
			builder.add(command);
		}
		return builder.build();
	}


	public static IChain onSuccess(final IChain chain, final ICommand... listeners) {
		return decorator(chain).addSuccessHandlers(listeners);
	}


	public static IChain onFailure(final IChain chain, final ICommand... listeners) {
		return decorator(chain).addFailureHandlers(listeners);
	}


	/**
	 * specify that the {@link IChain} will run on the given {@link ExecutorService}
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static IChain makeThreaded(final IChain chain, final ExecutorService executor) {
		return decorator(chain).executor(executor);
	}


	/**
	 * add undo support to a {@link IChain}
	 * @param chain
	 * @return
	 */
	public static IChain makeUndoable(final IChain chain) {
		return decorator(chain).undo();
	}


	/**
	 * enable visitor mode in a {@link IChain}
	 * @param chain
	 * @return
	 */
	public static IChain makeVisitable(final IChain chain) {
		return decorator(chain).visitable();
	}


	private static ChainDecorator decorator(final IChain chain) {
		return chain instanceof ChainDecorator ? (ChainDecorator)chain : new ChainDecorator(chain);
	}


	public static void invokeQuietly(final IChain chain)
		throws RuntimeException {

		try {
			chain.invoke();
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static void invokeQuietly(final IChain chain, final Object dto)
		throws RuntimeException {

		try {
			chain.invoke(dto);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * An empty {@link IChain} implementation
	 * 
	 * @author wassj
	 *
	 */
	private static class EmptyChain
		implements IChain {

		private final ILink head = Links.empty();


		public ILink head() {
			return head;
		}


		public void invoke() {
		}


		public void invoke(final Object dto) {
		}
	}
}
