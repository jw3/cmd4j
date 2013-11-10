package cmd4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.Internals.Chain.DefaultChain;
import cmd4j.Internals.Chain.EmptyChain;
import cmd4j.Internals.Chain.UndoableChainDecorator;
import cmd4j.Internals.Link;
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
		return new DefaultChain<O>(Link.create(command));
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
		 * set an individual overriding input for the tail link
		 * @param input
		 * @return
		 */
		public ChainBuilder input(final Object input) {
			if (tail == null) {
				throw new NullPointerException("chain builder was not initialized, tail is null");
			}
			tail.input(input);
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


	/**
	 * The context in which a {@link ICommand command} executes.
	 * 
	 * Represents 'a {@link ILink link} of a {@link IChain chain}'.  In other words; a part of a chain that
	 * is potentially connected to another link that is executed prior and likewise for one that executes after.
	 * 
	 * @author wassj
	 * 
	 * @input A Link can provide an overriding input that will be passed to commands executing withing the context of this Link.
	 * @concurrency A Link does not exhibit any concurrency behavior by default, but can be decorated to do so. 
	 * 
	 * @see Links
	 */
	public interface ILink
		extends Callable<Object> {

		/**
		 * get the {@link ILink link} to be executed after this 
		 * @return next {@link ILink link} or null
		 */
		ILink next();


		/**
		 * Data Transfer Object that is passed to {@link ICommand commands} within this link.
		 * This acts as an override to the {@link IChain chain} level input.
		 * @return Object the Data Transfer Object
		 */
		Object input();


		ILink input(Object input);


		/**
		 * the {@link ICommand command} in this link.  The actual command type will be inspected 
		 * at execution time.  It is possible that through the use of {@link ICommand3 command3}
		 * that this link could execute more than one command.  So this property should
		 * be thought of to represent the 'head' command for this link.
		 * @return {@link ICommand}
		 */
		ICommand cmd();


		ExecutorService executor();
	}
}
