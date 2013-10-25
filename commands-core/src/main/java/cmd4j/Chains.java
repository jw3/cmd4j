package cmd4j;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import cmd4j.IChain.IObservableChain;
import cmd4j.IChain.IReturningChain;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.Internals.Chain.ChainCallable;
import cmd4j.Internals.Chain.DefaultChain;
import cmd4j.Internals.Chain.EmptyChain;
import cmd4j.Internals.Chain.ReturningChain;
import cmd4j.Internals.Chain.UndoableChainDecorator;
import cmd4j.Internals.Link.LinkBuilder;

/**
 * Utility methods for {@link IChain chains}
 *
 * @author wassj
 *
 */
public enum Chains {
	/*singleton-enum*/;

	public static IObservableChain observable(final IChain chain) {
		return Internals.Chain.decorator(chain);
	}


	/**
	 * 
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static Future<Void> submit(final IChain chain, final ExecutorService executor) {
		return executor.submit(asCallable(chain));
	}


	/**
	 * 
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static <D> Future<Void> submit(final IChain chain, final D dto, final ExecutorService executor) {
		return executor.submit(asCallable(chain, dto));
	}


	/**
	 * 
	 * @param chain
	 * @return
	 */
	public static Callable<Void> asCallable(final IChain chain) {
		return new ChainCallable<Void, Void>(chain);
	}


	/**
	 * 
	 * @param chain
	 * @param dto
	 * @return
	 */
	public static <D> Callable<Void> asCallable(final IChain chain, final D dto) {
		return new ChainCallable<D, Void>(chain, dto);
	}


	/**
	 * creates a new {@link ChainBuilder}
	 */
	public static ChainBuilder builder() {
		return new ChainBuilder();
	}


	/**
	 * creates an empty {@link IChain chain} which can be used in any operation a normal chain would, but will not do anything
	 */
	public static IChain empty() {
		return new EmptyChain();
	}


	/**
	 * create a {@link IChain chain} from a {@link ILink link}
	 */
	public static IChain create(final ILink link) {
		return new DefaultChain(link);
	}


	/**
	 * create a {@link IChain chain} that contains the given {@link ICommand commands}
	 */
	public static IChain create(final Collection<ICommand> commands) {
		return create(commands.toArray(new ICommand[0]));
	}


	/**
	 * create a {@link IChain chain} that contains the given vararg {@link ICommand commands}
	 */
	public static IChain create(final ICommand... commands) {
		final ChainBuilder builder = Chains.builder();
		for (ICommand command : commands) {
			builder.add(command);
		}
		return builder.build();
	}


	public static <R> IReturningChain<R> create(final IReturningCommand<R> command) {
		return new ReturningChain<R>(command);
	}


	/**
	 * add undo support to a {@link IChain chain}
	 * @return the chain, decorated
	 */
	public static IChain makeUndoable(final IChain chain) {
		return new UndoableChainDecorator(chain);
	}


	/**
	 * invoke the chain converting any exceptions to a runtime exception
	 * @throws RuntimeException
	 */
	public static void invokeQuietly(final IChain chain)
		throws RuntimeException {

		try {
			chain.invoke();
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * invoke the chain converting any exceptions to a runtime exception
	 * @throws RuntimeException
	 */
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
	 * 
	 * 
	 * Builder pattern implementation for creating {@link IChain} objects
	 *
	 * @author wassj
	 *
	 */
	final public static class ChainBuilder {
		private LinkBuilder head;
		private LinkBuilder tail;
		private boolean visits;


		private ChainBuilder() {
		}


		/**
		 * init the builder with the passed builder as head (and tail for now)
		 * @param builder
		 * @return
		 */
		private ChainBuilder init(final LinkBuilder builder) {
			this.head = builder;
			this.tail = builder;
			return this;
		}


		public ChainBuilder visits(final boolean visits) {
			this.visits = visits;
			return this;
		}


		/**
		 * add the command to the end of the chain
		 * @param command
		 * @return
		 */
		public ChainBuilder add(final ICommand command) {
			if (command == null) {
				throw new IllegalArgumentException("command cannot be null");
			}

			// if created with the noarg create() method it will need initd on the first add
			if (head == null) {
				this.init(new LinkBuilder(command));
			}
			else {
				tail = tail != null ? tail.add(command) : new LinkBuilder(command);
			}
			return this;
		}


		/**
		 * set the executor for the tail link
		 * @param executor
		 * @return
		 */
		public ChainBuilder executor(final ExecutorService executor) {
			if (tail == null) {
				throw new NullPointerException("chain builder was not initialized, tail is null");
			}
			tail.executor(executor);
			return this;
		}


		/**
		 * set an individual overriding dto for the tail link
		 * @param dto
		 * @return
		 */
		public ChainBuilder dto(final Object dto) {
			if (tail == null) {
				throw new NullPointerException("chain builder was not initialized, tail is null");
			}
			tail.dto(dto);
			return this;
		}


		/**
		 * construct an {@link IChain} object from the {@link ICommand}s that have been added to this builder
		 * @return
		 */
		public IChain build() {
			if (head != null) {
				return Chains.create(head.build(visits));
			}
			return Chains.empty();
		}
	}
}
