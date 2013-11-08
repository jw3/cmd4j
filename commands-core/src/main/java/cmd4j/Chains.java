package cmd4j;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import cmd4j.IChain.IObservableChain;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.Internals.Chain.ChainCallable;
import cmd4j.Internals.Chain.DefaultChain;
import cmd4j.Internals.Chain.EmptyChain;
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

	public static <O> IObservableChain<O> observable(final IChain<O> chain) {
		return Internals.Chain.decorator(chain);
	}


	/**
	 * 
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static <O> Future<O> submit(final IChain<O> chain, final ExecutorService executor) {
		return executor.submit(asCallable(chain));
	}


	/**
	 * 
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static <I, O> Future<O> submit(final IChain<O> chain, final I dto, final ExecutorService executor) {
		return executor.submit(asCallable(chain, dto));
	}


	/**
	 * 
	 * @param chain
	 * @return
	 */
	public static <O> Callable<O> asCallable(final IChain<O> chain) {
		return new ChainCallable<Void, O>(chain);
	}


	/**
	 * 
	 * @param chain
	 * @param dto
	 * @return
	 */
	public static <I, O> Callable<O> asCallable(final IChain<O> chain, final I dto) {
		return new ChainCallable<I, O>(chain, dto);
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
	public static IChain<Void> empty() {
		return new EmptyChain();
	}


	/**
	 * create a {@link IChain chain} that contains the given {@link ICommand commands}
	 */
	public static IChain<Void> create(final Collection<ICommand> commands) {
		return create(commands.toArray(new ICommand[0]));
	}


	/**
	 * create a {@link IChain chain} that contains the given vararg {@link ICommand commands}
	 */
	public static IChain<Void> create(final ICommand... commands) {
		final ChainBuilder builder = Chains.builder();
		for (ICommand command : commands) {
			builder.add(command);
		}
		return builder.build();
	}


	/**
	 * create a {@link IChain chain} that contains the given vararg {@link ICommand commands}
	 */
	public static <O> IChain<O> create(final IReturningCommand<O> command) {
		return new DefaultChain<O>(command);
	}


	/**
	 * add undo support to a {@link IChain chain}
	 * @return the chain, decorated
	 */
	public static IChain<Void> makeUndoable(final IChain<Void> chain) {
		return new UndoableChainDecorator<Void>(chain);
	}


	/**
	 * invoke a {@link IChain chain}, returning a value
	 * @param chain
	 * @throws Exception
	 */
	public static <O> O invoke(final IChain<O> chain)
		throws Exception {

		return invoke(chain, null);
	}


	/**
	 * invoke a {@link IChain chain} with a dto, returning a value
	 * @param chain
	 * @param dto
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <O> O invoke(final IChain<O> chain, final Object dto)
		throws Exception {

		/*if (chain.head() != null) {
			final Linker linker = new Linker(chain.head(), dto);
			return (R)Executors2.sameThreadExecutor().submit(linker).get();
		}*/
		return (O)Internals.Link.invokeCommand(chain, dto, false);
	}


	/**
	 * invoke the chain converting any exceptions to a runtime exception
	 * @throws RuntimeException
	 */
	public static void invokeQuietly(final IChain<Void> chain)
		throws RuntimeException {

		try {
			invoke(chain);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * invoke the chain converting any exceptions to a runtime exception
	 * @throws RuntimeException
	 */
	public static void invokeQuietly(final IChain<Void> chain, final Object dto)
		throws RuntimeException {

		try {
			invoke(chain, dto);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * create a {@link IChain chain} from a {@link ILink link}
	 */
	public static IChain<Void> create(final ILink link) {
		return new DefaultChain<Void>(link);
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
		public IChain<Void> build() {
			if (head != null) {
				return Chains.create(head.build(visits));
			}
			return Chains.empty();
		}
	}
}
