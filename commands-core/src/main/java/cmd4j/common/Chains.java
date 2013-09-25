package cmd4j.common;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.ILink;
import cmd4j.common.Links.LinkBuilder;
import cmd4j.internal.Callables;
import cmd4j.internal.ChainDecorator;
import cmd4j.internal.Linkers;
import cmd4j.internal.Linkers.ILinker;

/**
 * Utility methods for {@link IChain}s
 *
 * @author wassj
 *
 */
public enum Chains {
	/*singleton-enum*/;

	/**
	 * creates a new {@link ChainBuilder}
	 * @return {@link ChainBuilder} a new builder
	 */
	public static ChainBuilder builder() {
		return new ChainBuilder();
	}


	/**
	 * creates an empty {@link IChain} which can be used in any operation a normal chain would, but will not do anything
	 * @return {@link IChain} the empty chain
	 */
	public static IChain empty() {
		return new EmptyChain();
	}


	public static IChain create(final ILink link) {
		return new DefaultChain(link);
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
		final ChainBuilder builder = Chains.builder();
		for (ICommand command : commands) {
			builder.add(command);
		}
		return builder.build();
	}


	public static IChain before(final IChain chain, final ICommand... listeners) {
		return decorator(chain).before(listeners);
	}


	public static IChain whenDone(final IChain chain, final ICommand... listeners) {
		return decorator(chain).onFinished(listeners);
	}


	public static IChain onSuccess(final IChain chain, final ICommand... listeners) {
		return decorator(chain).onSuccess(listeners);
	}


	public static IChain onFailure(final IChain chain, final ICommand... listeners) {
		return decorator(chain).onFailure(listeners);
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


	/**
	 * invoke the chain converting any exceptions to a runtime exception
	 * @param chain
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
	 * @param chain
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
	 * Builder pattern implementation for creating {@link IChain} objects
	 * 
	 * There are two types of {@link IChain}s that can be created, static and dynamic.  This builder will assemble a static 
	 *
	 * @author wassj
	 *
	 */
	final public static class ChainBuilder {
		private LinkBuilder head;
		private LinkBuilder tail;


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
			return this.buildImpl();
		}


		/**
		 * @param executor
		 * @return
		 */
		public IChain build(final ExecutorService executor) {
			if (executor == null) {
				throw new IllegalArgumentException("executor cannot be null");
			}
			return Chains.makeThreaded(this.buildImpl(), executor);
		}


		private IChain buildImpl() {
			if (head != null) {
				return Chains.create(head.build());
			}
			return Chains.empty();
		}
	}


	/******************************************************************************
	 * 
	 * 
	 * 
	 * begin private implementation details
	 * 
	 * 
	 * 
	 ******************************************************************************/

	private static ChainDecorator decorator(final IChain chain) {
		return chain instanceof ChainDecorator ? (ChainDecorator)chain : new ChainDecorator(chain);
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


	private static class DefaultChain
		implements IChain {

		private final ILink head;


		public DefaultChain(final ILink head) {
			this.head = head;
		}


		public ILink head() {
			return this.head;
		}


		public void invoke()
			throws Exception {

			this.invoke(null);
		}


		public void invoke(final Object dto)
			throws Exception {

			final ILinker linker = Linkers.create(this.head());
			final Callable<Void> callableLinker = Callables.linker(linker, dto);
			Executors2.sameThreadExecutor().submit(callableLinker).get();
		}
	}
}
