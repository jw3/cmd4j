package cmd4j.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import cmd4j.IChain;
import cmd4j.IChain.IObservableChain;
import cmd4j.ICommand;
import cmd4j.ILink;
import cmd4j.common.Links.LinkBuilder;

/**
 * Utility methods for {@link IChain chains}
 *
 * @author wassj
 *
 */
public enum Chains {
	/*singleton-enum*/;

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


	/**
	 * decorate a chain with observable capability
	 * @param chain
	 * @return
	 */
	public static IObservableChain observable(final IChain chain) {
		return decorator(chain);
	}


	/**
	 * add undo support to a {@link IChain chain}
	 * @return the chain, decorated
	 */
	public static IChain makeUndoable(final IChain chain) {
		return null;//decorator(chain).undo();
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


	/******************************************************************************
	 * 
	 * 
	 * 
	 * begin private implementation details
	 * 
	 * 
	 * 
	 ******************************************************************************/

	private static IObservableChain decorator(final IChain chain) {
		return chain instanceof IObservableChain ? (IObservableChain)chain : new ObservableChainDecorator(chain);
	}


	/**
	 * An empty {@link IChain} implementation
	 * 
	 * @author wassj
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


	/**
	 * the default {@link IChain} implementation
	 *
	 * @author wassj
	 */
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

			final Linker linker = new Linker(this.head(), dto);
			Executors2.sameThreadExecutor().submit(linker).get();
		}
	}


	public static class ChainCallable<D, R>
		implements Callable<R> {

		private final IChain chain;
		private final D dto;


		public ChainCallable(final IChain chain) {
			this(chain, null);
		}


		public ChainCallable(final IChain chain, final D dto) {
			this.chain = chain;
			this.dto = dto;
		}


		public R call()
			throws Exception {

			chain.invoke(dto);
			return null;
		}
	}


	/**
	 * A {@link ILinker linker} is a traverser of {@link ILink links} in an {@link IChain chain}.  It controls the 
	 * execution flow from link to link and ensures that each command is run by the appropriate {@link ExecutorService executor}.
	 * 
	 * @author wassj
	 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
	 *
	 */
	private static class Linker
		implements Callable<Void> {

		private final ILink head;
		private final Object dto;


		public Linker(final ILink head, final Object dto) {
			this.head = head;
			this.dto = dto;
		}


		public ILink head() {
			return head;
		}


		public Void call()
			throws Exception {

			ILink next = head();
			while (next != null) {
				next = callImpl(next);
			}
			return null;
		}


		private ILink callImpl(final ILink link)
			throws Exception {

			if (dto != null && link.dto() == null) {
				link.dto(dto);
			}
			final ExecutorService executor = link.executor();
			if (executor != null) {
				final Future<ILink> future = executor.submit(link);
				return future.get();
			}
			return link.call();
		}
	}


	/**
	 *
	 * @author wassj
	 */
	public static class ObservableChainDecorator
		implements IObservableChain, IChainDecorator {

		private final List<ICommand> beforeHandlers = new LinkedList<ICommand>();
		private final List<ICommand> afterHandlers = new LinkedList<ICommand>();
		private final List<ICommand> successHandlers = new LinkedList<ICommand>();
		private final List<ICommand> failureHandlers = new LinkedList<ICommand>();

		private final IChain chain;


		public ObservableChainDecorator(final IChain chain) {
			this.chain = chain;
		}


		public IChain getDecorating() {
			return chain;
		}


		public IObservableChain before(final ICommand... commands) {
			beforeHandlers.addAll(Arrays.asList(commands));
			return this;
		}


		public IObservableChain after(final ICommand... commands) {
			afterHandlers.addAll(Arrays.asList(commands));
			return this;
		}


		public IObservableChain onSuccess(final ICommand... commands) {
			successHandlers.addAll(Arrays.asList(commands));
			return this;
		}


		public IObservableChain onFailure(final ICommand... commands) {
			failureHandlers.addAll(Arrays.asList(commands));
			return this;
		}


		/*
		 * 
		 * IChain impl
		 * 
		 */
		public ILink head() {
			return chain.head();
		}


		public void invoke()
			throws Exception {

			this.invoke(null);
		}


		public void invoke(final Object dto)
			throws Exception {

			executeHandlers(beforeHandlers, dto);

			try {
				chain.invoke(dto);
				executeHandlers(successHandlers, dto);
			}
			catch (ExecutionException e) {
				executeHandlers(failureHandlers, e.getCause());
				throw e;
			}
			catch (InterruptedException e) {
				executeHandlers(failureHandlers, e);
				throw e;
			}
			finally {
				executeHandlers(afterHandlers, dto);
			}
		}


		private void executeHandlers(final List<ICommand> commands, final Object dto) {
			try {
				Chains.create(commands).invoke(dto);
			}
			catch (Throwable t) {
				// REVISIT the show must go on
				t.printStackTrace();
			}
		}
	}


	private interface IChainDecorator {
		IChain getDecorating();
	}
}
