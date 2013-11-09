package cmd4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import cmd4j.ICommand.IReturningCommand;
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

	/**
	 * creates a new {@link ChainBuilder}
	 */
	public static ChainBuilder builder() {
		return new ChainBuilder();
	}


	/**
	 * create a {@link IChain chain} that contains the given vararg {@link ICommand commands}
	 */
	public static IChain<Void> create(final ICommand... commands) {
		return create(Arrays.asList(commands));
	}


	/**
	 * create a {@link IChain chain} that contains the given {@link ICommand commands}
	 */
	public static IChain<Void> create(final Collection<ICommand> commands) {
		final ChainBuilder builder = Chains.builder();
		for (final ICommand command : commands) {
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
	public static <O> IChain<O> undoable(final IChain<O> chain) {
		return new UndoableChainDecorator<O>(chain);
	}


	/**
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
				return new DefaultChain<Void>(head.build(visits));
			}
			return new EmptyChain();
		}
	}
}
