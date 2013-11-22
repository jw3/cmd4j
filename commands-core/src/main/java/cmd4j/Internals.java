package cmd4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cmd4j.Chains.ChainBuilder;
import cmd4j.Chains.ILink;
import cmd4j.IChain.IObservableChain;
import cmd4j.IChain.IUndoChain;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IObservableCommand;
import cmd4j.ICommand.IObservableStateCommand;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.ICommand.IStateCommand;
import cmd4j.ICommand.IStateCommand.IStateCommand1;
import cmd4j.ICommand.IStateCommand.IStateCommand2;
import cmd4j.Internals.Chain.IChainDecorator;
import cmd4j.Internals.Chain.IDecorator;
import cmd4j.Internals.Command.ICommandProxy;
import cmd4j.Observers.IObservable;

/**
 * Non-API Implementations; Not for public consumption
 *
 * @author wassj
 *
 */
enum Internals {
	/*noinstance*/;

	/******************************************************************************
	 * 
	 * 
	 * 
	 * {@link ICommand} implementation details
	 * 
	 * 
	 * 
	 ******************************************************************************/
	enum Command {
		/*noinstance*/;

		static class CommandCallable<R>
			implements Callable<R> {

			private final IReturningCommand<R> command;
			private final Object input;


			public CommandCallable(final IReturningCommand<R> command) {
				this(command, null);
			}


			public CommandCallable(final IReturningCommand<R> command, final Object input) {
				this.command = command;
				this.input = input;
			}


			public R call()
				throws Exception {

				return Chains.create(command).invoke(input);
			}
		}


		/**
		 * {@link ICommand command} that delegates to another
		 *
		 * @author wassj
		 * @param <C>
		 */
		interface ICommandProxy<C extends ICommand>
			extends ICommand {

			IInputCommand<C> command();
		}
	}


	/******************************************************************************
	 * 
	 * 
	 * 
	 * {@link ILink} implementation details
	 * 
	 * 
	 * 
	 ******************************************************************************/
	enum Link {
		/*noinstance*/;

		static ILink empty() {
			return new EmptyLink();
		}


		static ILink create(final ICommand command) {
			return new LinkBuilder(command).build();
		}


		static ILink undo(final ILink link) {
			return new LinkUndoDecorator(link);
		}


		/**
		 * Provides the context in which a {@link ICommand command} executes.  
		 * Can combine together with other {@link ILink links} to form a chain.
		 * 
		 * @author wassj
		 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
		 */
		static class DefaultLink
			implements ILink {

			private final Returns returns = new Returns();
			private final ICommand command;
			private final ILink next;

			private Object input;
			private ExecutorService executor;
			private boolean visits;
			private boolean postSwap;


			public DefaultLink(final ICommand command, final ILink next) {
				this.command = command;
				this.next = next;
			}


			public Object input() {
				return input;
			}


			public DefaultLink input(final Object input) {
				this.input = input;
				return this;
			}


			public ICommand cmd() {
				return command;
			}


			public ILink next() {
				return next;
			}


			public ExecutorService executor() {
				return executor;
			}


			public DefaultLink executor(final ExecutorService executor) {
				this.executor = executor;
				return this;
			}


			public DefaultLink visits(final boolean visits) {
				this.visits = visits;
				return this;
			}


			public boolean postSwap() {
				return postSwap;
			}


			public DefaultLink postSwap(final boolean postSwap) {
				this.postSwap = postSwap;
				return this;
			}


			/**
			 * Execute the {@link ICommand} and return the next {@link ILink}
			 */
			public Object call()
				throws Exception {

				ICommand command = cmd();
				while (command != null) {
					command = invokeCommand(command, input, returns, visits);
				}
				return returns.get();
			}
		}


		/**
		 * Builder pattern implementation for {@link ILink links}
		 * 
		 * @author wassj
		 */
		static class LinkBuilder {
			private final ICommand command;
			private LinkBuilder next;

			private ExecutorService executor;
			private Object input;

			private boolean postSwap;


			/**
			 * creates a new builder. package private as only the {@link ChainBuilder} should create these
			 * @param command
			 */
			LinkBuilder(final ICommand command) {
				this.command = command;
			}


			/**
			 * sets the executor for the link
			 * @param executor
			 * @return
			 */
			LinkBuilder executor(final ExecutorService executor) {
				this.executor = executor;
				return this;
			}


			LinkBuilder add(final ICommand command) {
				next = new LinkBuilder(command);
				return next;
			}


			LinkBuilder input(final Object input) {
				this.input = input;
				return this;
			}


			LinkBuilder postSwap(final boolean postSwap) {
				this.postSwap = postSwap;
				return this;
			}


			ILink build() {
				return build(false);
			}


			ILink build(final boolean visits) {
				return new DefaultLink(command, next != null ? next.build(visits) : null).executor(executor).input(input).visits(visits).postSwap(postSwap);
			}
		}


		/**
		 * An empty {@link ILink link}
		 *
		 * @author wassj
		 *
		 */
		static class EmptyLink
			implements ILink {

			public ILink next() {
				return null;
			}


			public Object input() {
				return null;
			}


			public ILink input(Object input) {
				return this;
			}


			public boolean postSwap() {
				return false;
			}


			public ICommand cmd() {
				return new ICommand1() {
					public void invoke() {
					}
				};
			}


			public ExecutorService executor() {
				return null;
			}


			public ILink call() {
				return next();
			}
		}


		// REVISIT should this support input mismatch ignores?
		static class LinkUndoDecorator
			implements ILink {

			private final ILink link;


			public LinkUndoDecorator(final ILink link) {
				this.link = link;
			}


			public ILink next() {
				return link.next();
			}


			public Object input() {
				return link.input();
			}


			public ILink input(Object input) {
				return link.input(input);
			}


			public boolean postSwap() {
				return false;
			}


			public ICommand cmd() {
				return link.cmd();
			}


			public ExecutorService executor() {
				return link.executor();
			}


			@Override
			public Object call()
				throws Exception {

				final Returns returns = new Returns();
				ICommand command = cmd();
				while (command != null) {
					command = invokeCommand(command, input(), returns, false, true);
				}
				return returns.get();
			}
		}


		/*
		 * 
		 * utils
		 * 
		 * 
		 */

		/**
		 * util for executing and handling any return value from a given {@link ICommand}
		 * @param command
		 * @param input
		 * @return
		 * @throws Exception
		 */
		static ICommand invokeCommand(final ICommand command, final Object input, final Returns output, final boolean visits)
			throws Exception {

			return invokeCommand(command, input, output, visits, false);
		}


		@SuppressWarnings({"unchecked", "rawtypes"})
		/// safely suppressed here: we do some extra checking to ensure the input fits in the invocation
		static ICommand invokeCommand(final ICommand command, final Object input, final Returns output, final boolean visits, final boolean undo)
			throws Exception {

			try {
				if (command instanceof ICommandProxy) {
					return ((ICommandProxy)command).command();
				}
				else if (!undo) {
					if (command instanceof IStateCommand2<?>) {
						return ((IStateCommand2)command).invoke(input);
					}
					else if (command instanceof IStateCommand1) {
						return ((IStateCommand1)command).invoke();
					}
					else if (command instanceof ICommand4<?, ?>) {
						output.set(((ICommand4)command).invoke(input));
					}
					else if (command instanceof ICommand2<?>) {
						((ICommand2)command).invoke(input);
					}
					else if (command instanceof ICommand3<?>) {
						output.set(((ICommand3)command).invoke());
					}
					else if (command instanceof ICommand1) {
						((ICommand1)command).invoke();
					}
				}
				else {
					if (command instanceof ICommand4.IUndo<?, ?>) {
						output.set(((ICommand4.IUndo)command).undo(input));
					}
					else if (command instanceof ICommand2.IUndo<?>) {
						((ICommand2.IUndo)command).undo(input);
					}
					else if (command instanceof ICommand3.IUndo<?>) {
						output.set(((ICommand3.IUndo)command).undo());
					}
					else if (command instanceof ICommand1.IUndo) {
						((ICommand1.IUndo)command).undo();
					}
				}
			}
			catch (final ClassCastException e) {
				if (!visits) {
					throw new IllegalArgumentException("input does not fit");
				}
			}
			return null;
		}
	}


	/******************************************************************************
	 * 
	 * 
	 * 
	 * {@link IChain} implementation details
	 * 
	 * 
	 * 
	 ******************************************************************************/
	enum Chain {
		/*noinstance*/;

		/**
		 * An empty {@link IChain} implementation
		 * 
		 * @author wassj
		 */
		static class EmptyChain<O>
			implements IChain<O> {

			private final ILink head = Link.empty();


			public ILink head() {
				return head;
			}


			public O invoke() {
				return null;
			}


			public O invoke(final Object input) {
				return null;
			}
		}


		/**
		 * the default {@link IChain} implementation
		 *
		 * @author wassj
		 */
		static class DefaultChain<O>
			implements IChain<O> {

			private final ILink link;


			DefaultChain(final ILink link) {
				this.link = link;
			}


			public O invoke()
				throws Exception {

				return this.invoke(null);
			}


			public O invoke(final Object input)
				throws Exception {

				//				final Linker linker = new Linker(this.head(), input);
				//				return (O)MoreExecutors.sameThreadExecutor().submit(linker).get();

				final Input inputs = new Input(input);
				final Returns returns = new Returns();
				ILink next = head();
				while (next != null) {
					next = callImpl(next, inputs, returns);
				}
				return (O)returns.get();
			}


			protected ILink callImpl(final ILink link, final Input inputs, final Returns returns)
				throws Exception {

				if (!inputs.isNull() && link.input() == null) {
					link.input(inputs.get());
				}
				final ExecutorService executor = link.executor();
				if (executor == null) {
					returns.set(link.call());
				}
				else {
					returns.set(executor.submit(link).get());
				}
				if (link.postSwap()) {
					inputs.set(returns.get());
				}
				return link.next();
			}


			public ILink head() {
				return link;
			}
		}


		static class ChainCallable<O>
			implements Callable<O> {

			private final IChain<O> chain;
			private final Object input;


			public ChainCallable(final IChain<O> chain) {
				this(chain, null);
			}


			public ChainCallable(final IChain<O> chain, final Object input) {
				this.chain = chain;
				this.input = input;
			}


			public O call()
				throws Exception {

				return chain.invoke(input);
			}
		}


		/**
		 *
		 * @author wassj
		 * @param <T>
		 */
		interface IDecorator<T> {
			T decorating();
		}


		/**
		 *
		 * @author wassj
		 */
		interface IChainDecorator<O>
			extends IDecorator<IChain<O>>, IChain<O> {
		}


		/**
		 *
		 * @author wassj
		 */
		static class UndoableChainDecorator<O>
			extends DefaultChain<O>
			implements IChainDecorator<O>, IUndoChain<O> {

			private final IChain<O> chain;


			public UndoableChainDecorator(final IChain<O> chain) {
				super(chain.head());
				this.chain = chain;
			}


			public IChain<O> decorating() {
				return chain;
			}


			public ILink head() {
				return chain.head();
			}


			public O undo()
				throws Exception {

				return this.undo(null);
			}


			public O undo(final Object input)
				throws Exception {

				//				final Linker linker = new UndoLinker(this.head(), input);
				//				return (O)MoreExecutors.sameThreadExecutor().submit(linker).get();

				final Input inputs = new Input(input);
				final Returns returns = new Returns();
				ILink next = head();
				while (next != null) {
					next = callImpl(Link.undo(next), inputs, returns);
				}
				return (O)returns.get();
			}
		}
	}


	/******************************************************************************
	 * 
	 * 
	 * 
	 * {@link IObservable} implementation details
	 * 
	 * 
	 * 
	 ******************************************************************************/
	enum Observer {
		/*noinstance*/;

		/**
		 * cast to or create an {@link IObservableChain} from the passed {@link IChain} instance
		 * @param chain
		 * @return
		 */
		static <O> IObservableChain<O> decorator(final IChain<O> chain) {
			return chain instanceof IObservableChain<?> ? (IObservableChain<O>)chain : new ObservableChainDecorator<O>(chain);
		}


		/**
		 * cast to or create an {@link IObservableCommand} from the passed {@link ICommand} instance
		 * @param command
		 * @return
		 */
		static <O> IObservableCommand<O> decorator(final IReturningCommand<O> command) {
			return command instanceof IObservableCommand<?> ? (IObservableCommand<O>)command : new ObservableCommandDecorator<O>(command);
		}


		/**
		 * cast to or create an {@link IObservableStateCommand} from the passed {@link IStateCommand} instance
		 * @param chain
		 * @return
		 */
		static IObservableStateCommand decorator(final IStateCommand command) {
			return command instanceof IObservableStateCommand ? (IObservableStateCommand)command : new StateCommandDecorator(command);
		}


		/**
		 *
		 * @author wassj
		 */
		static class StateCommandDecorator
			extends AbstractObservable<ICommand, IObservableStateCommand, IStateCommand>
			implements IObservableStateCommand, IStateCommand, IStateCommand2<Object> {

			private ICommand executing;


			public StateCommandDecorator(final IStateCommand command) {
				super(command);
			}


			protected ICommand invokeImpl(final Object input)
				throws Exception {

				return Link.invokeCommand(executing, input, Returns.VOID, true);
			}


			protected ICommand undoImpl(Object input)
				throws Exception {

				return Link.invokeCommand(executing, input, Returns.VOID, true, true);
			}


			@Override
			public ICommand invoke(final Object input)
				throws Exception {

				executing = this.decorating();
				while (executing != null) {
					try {
						executeHandlers(beforeHandlers, input);
						executing = invokeImpl(input);
						/// executeHandlers(resultsHandlers(), returned);
						executeHandlers(successHandlers, input);
					}
					catch (final ExecutionException e) {
						executeHandlers(failureHandlers, e.getCause());
						throw e;
					}
					catch (final Exception e) {
						executeHandlers(failureHandlers, e);
						throw e;
					}
					finally {
						executeHandlers(afterHandlers, input);
					}
				}
				return null;
			}
		}


		/**
		 *
		 * @author wassj
		 */
		static class ObservableCommandDecorator<O>
			extends AbstractObservable<O, IObservableCommand<O>, IReturningCommand<O>>
			implements IObservableCommand<O>, ICommand4<Object, O> {

			public ObservableCommandDecorator(final IReturningCommand<O> command) {
				super(command);
			}


			protected O invokeImpl(final Object input)
				throws Exception {

				return Chains.create(this.decorating()).invoke(input);
			}


			protected O undoImpl(Object input)
				throws Exception {

				if (this.decorating() instanceof IUndoCommand) {
					return Chains.undoable(Chains.create(this.decorating())).undo(input);
				}
				return this.invokeImpl(input);
			}
		}


		/**
		*
		* @author wassj
		*/
		static class ObservableChainDecorator<O>
			extends AbstractObservable<O, IObservableChain<O>, IChain<O>>
			implements IObservableChain<O>, IChainDecorator<O> {

			public ObservableChainDecorator(final IChain<O> chain) {
				super(chain);
			}


			public ILink head() {
				return this.decorating().head();
			}


			protected O invokeImpl(final Object input)
				throws Exception {

				return this.decorating().invoke(input);
			}


			protected O undoImpl(Object input)
				throws Exception {

				if (this.decorating() instanceof IUndoChain<?>) {
					((IUndoChain<O>)this.decorating()).undo(input);
				}
				return Chains.undoable(this.decorating()).undo(input);
			}
		}


		/**
		 * abstract superclass of the {@link IObservable} implementations contains all the core logic;
		 * the subclasses exist to provide a distinction between {@link IChain} and {@link ICommand} observables
		 * 
		 * @author wassj
		 *
		 * @param <O> the output type
		 * @param <T> the IObservable type
		 * @param <C> the ICommand type
		 */
		@SuppressWarnings("unchecked")
		/// EXPLAIN there are some wild generic parms here resulting from the IObservable interface being typed with a generic form of itself
		abstract static class AbstractObservable<O, T extends IObservable<?>, C extends ICommand>
			implements IObservable<T>, IDecorator<C>, ICommand4.IUndo<Object, O> {

			protected final List<ICommand> afterHandlers = new LinkedList<ICommand>();
			protected final List<ICommand> beforeHandlers = new LinkedList<ICommand>();
			protected final List<ICommand> resultsHandlers = new LinkedList<ICommand>();
			protected final List<ICommand> successHandlers = new LinkedList<ICommand>();
			protected final List<ICommand> failureHandlers = new LinkedList<ICommand>();

			private final C command;


			public AbstractObservable(final C command) {
				this.command = command;
			}


			public C decorating() {
				return command;
			}


			abstract protected O invokeImpl(final Object input)
				throws Exception;


			abstract protected O undoImpl(final Object input)
				throws Exception;


			public O invoke()
				throws Exception {

				return this.invoke(null);
			}


			public O invoke(final Object input)
				throws Exception {

				try {
					final O returned;
					executeHandlers(beforeHandlers, input);
					returned = invokeImpl(input);
					executeHandlers(resultsHandlers, returned);
					executeHandlers(successHandlers, input);
					return returned;
				}
				catch (final ExecutionException e) {
					executeHandlers(failureHandlers, e.getCause());
					throw e;
				}
				catch (final Exception e) {
					executeHandlers(failureHandlers, e);
					throw e;
				}
				finally {
					executeHandlers(afterHandlers, input);
				}
			}


			public O undo()
				throws Exception {

				return this.undo(null);
			}


			public O undo(final Object input)
				throws Exception {

				try {
					final O returned;
					executeHandlers(beforeHandlers, input);
					returned = undoImpl(input);
					executeHandlers(resultsHandlers, returned);
					executeHandlers(successHandlers, input);
					return returned;
				}
				catch (final ExecutionException e) {
					executeHandlers(failureHandlers, e.getCause());
					throw e;
				}
				catch (final Exception e) {
					executeHandlers(failureHandlers, e);
					throw e;
				}
				finally {
					executeHandlers(afterHandlers, input);
				}
			}


			public T before(final ICommand... commands) {
				beforeHandlers.addAll(Arrays.asList(commands));
				return (T)this;
			}


			public T after(final ICommand... commands) {
				afterHandlers.addAll(Arrays.asList(commands));
				return (T)this;
			}


			public T results(final ICommand... commands) {
				resultsHandlers.addAll(Arrays.asList(commands));
				return (T)this;
			}


			public T onSuccess(final ICommand... commands) {
				successHandlers.addAll(Arrays.asList(commands));
				return (T)this;
			}


			public T onFailure(final ICommand... commands) {
				failureHandlers.addAll(Arrays.asList(commands));
				return (T)this;
			}


			protected void executeHandlers(final List<ICommand> commands, final Object input) {
				try {
					Chains.create(commands).invoke(input);
				}
				catch (final Throwable t) {
					// REVISIT the show must go on
					t.printStackTrace();
				}
			}
		}
	}


	/******************************************************************************
	 * 
	 * 
	 * 
	 * {@link ExecutorService} related implementation details
	 * 
	 * 
	 * 
	 ******************************************************************************/
	enum Executor {
		/*noinstance*/;

		/**
		 * A basic implementation of an ExecutorService that runs things on the EDT
		 * 
		 * @author wassj
		 */
		static class EventDispatchExecutor
			extends AbstractExecutorService {

			private final Logger logger = LoggerFactory.getLogger(EventDispatchExecutor.class);
			private volatile boolean shutdown = false;


			public boolean isShutdown() {
				return shutdown;
			}


			public void shutdown() {
				shutdown = true;
			}


			public List<Runnable> shutdownNow() {
				return Collections.emptyList();
			}


			public boolean isTerminated() {
				return shutdown;
			}


			public boolean awaitTermination(final long timeout, final TimeUnit unit) {
				return false;
			}


			public void execute(final Runnable command) {
				try {
					if (SwingUtilities.isEventDispatchThread()) {
						/*--------------------------------------------------------------------------------------------------------------------------
						 * the safest thing to do here is to run the command and return.  on the surface it might not appear to be fair
						 * to run the command without scheduling it with the EventQueue, but it is. the whole reason we got this time (right now) 
						 * on the EDT was to run the command, it doesnt make sense to give up our position by scheduling it at the end of the queue.
						 * besides like i mentioned it is not inherently safe to schedule from here.
						 *--------------------------------------------------------------------------------------------------------------------------
						 */
						command.run();
					}
					else {
						SwingUtilities.invokeAndWait(command);
					}
				}
				catch (Throwable e) {
					// we shouldnt get errors here as we handle them all within the chains
					logger.warn("caught something in an ede", e);
				}
			}
		}
	}


	/**
	 * util class for passing values as parameter
	 * @author wassj
	 */
	static class Variable {
		private Object value;


		public Object get() {
			return value;
		}


		public void set(final Object value) {
			this.value = value;
		}


		public boolean isNull() {
			return null == value;
		}


		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}


	/**
	 * {@link Variable} for return values
	 * @author wassj
	 */
	static class Returns
		extends Variable {

		public static Returns VOID = new Returns() {
			@Override
			public Object get() {
				return null;
			}


			@Override
			public void set(Object value) {
			}
		};
	}


	/**
	 * {@link Variable} for input values
	 * @author wassj
	 */
	static class Input
		extends Variable {

		public Input() {
		}


		public Input(final Object value) {
			super.set(value);
		}
	}
}
