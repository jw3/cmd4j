package cmd4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cmd4j.Chains.IBaseFluentChainBuilder;
import cmd4j.Chains.IChainBuilder;
import cmd4j.Chains.ILink;
import cmd4j.Chains.IReturningChainBuilder;
import cmd4j.Commands.Variable;
import cmd4j.IChain.IObservableChain;
import cmd4j.IChain.IUndoChain;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IFunction;
import cmd4j.ICommand.IObservableCommand;
import cmd4j.ICommand.IObservableStateCommand;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.ICommand.IStateCommand;
import cmd4j.ICommand.IStateCommand.IStateCommand1;
import cmd4j.ICommand.IStateCommand.IStateCommand2;
import cmd4j.Internals.Chain.DefaultChain;
import cmd4j.Internals.Chain.DefaultChain.ReturningChain;
import cmd4j.Internals.Chain.EmptyChain;
import cmd4j.Internals.Chain.IChainDecorator;
import cmd4j.Internals.Chain.IDecorator;
import cmd4j.Internals.Command.DefaultCallFactory;
import cmd4j.Internals.Command.ICommandCallFactory;
import cmd4j.Internals.Link.LinkBuilder;
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


		/**
		 * factory for {@link Callable} implementation that will execute the specified {@link ICommand}
		 * @author wassj
		 * @param <O>
		 */
		interface ICommandCallFactory<O> {
			/**
			 * create a {@link Callable}
			 * @param head
			 * @param input
			 * @param returns
			 * @param called
			 * @return
			 */
			Callable<O> create(ICommand head, Input input, Returns returns, Called called);
		}


		/**
		 * default implementation of {@link ICommandCallFactory}
		 * @author wassj
		 */
		static class DefaultCallFactory
			implements ICommandCallFactory<Void> {

			private final boolean visit;
			private final boolean undo;


			public DefaultCallFactory() {
				this(false, false);
			}


			public DefaultCallFactory(final boolean visit, final boolean undo) {
				this.visit = visit;
				this.undo = undo;
			}


			public Callable<Void> create(final ICommand head, final Input input, final Returns returns, final Called called) {
				return new Callable<Void>() {
					public Void call()
						throws Exception {

						ICommand command = head;
						while (command != null) {
							command = invoke(command, input.get(), returns, called, visit, undo);
						}
						return null;
					}
				};
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
		static ICommand invoke(final ICommand command, final Object input, final Returns output, final Called called, final boolean visits)
			throws Exception {

			return invoke(command, input, output, called, visits, false);
		}


		@SuppressWarnings({"unchecked", "rawtypes"})
		/// safely suppressed here: we do some extra checking to ensure the input fits in the invocation
		static ICommand invoke(final ICommand command, final Object input, final Returns output, final Called called, final boolean visits, final boolean undo)
			throws Exception {

			// will unset later if calling does not occur
			called.set(true);

			try {
				if (command instanceof ICommandProxy) {
					called.set(false);
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
					called.set(false);
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


		/**
		 * Provides the context in which a {@link ICommand command} executes.  
		 * Can combine together with other {@link ILink links} to form a chain.
		 * 
		 * @author wassj
		 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
		 */
		static class DefaultLink
			implements ILink {

			private final ICommand command;
			private final ILink next;

			private Object input;
			private ExecutorService executor;


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


			ILink build() {
				return new DefaultLink(command, next != null ? next.build() : null).executor(executor).input(input);
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

			private final Returns returns = new Returns();
			private final ILink link;

			private ICommandCallFactory<?> callFactory = new DefaultCallFactory();


			DefaultChain(final ILink link) {
				this.link = link;
			}


			public Object returns() {
				return returns.get();
			}


			public ICommandCallFactory<?> callFactory() {
				return callFactory;
			}


			public DefaultChain<O> callFactory(final ICommandCallFactory<?> callFactory) {
				this.callFactory = callFactory;
				return this;
			}


			public O invoke()
				throws Exception {

				return this.invoke(null);
			}


			public O invoke(final Object input)
				throws Exception {

				final Input inputs = new Input(input);
				final Called called = new Called();
				ILink next = head();
				while (next != null) {
					next = callImpl(next, inputs, returns, called, callFactory);
				}
				return (O)returns();
			}


			protected ILink callImpl(final ILink link, final Input inputs, final Returns returns, final Called called, final ICommandCallFactory<?> callFactory)
				throws Exception {

				if (!inputs.isNull() && link.input() == null) {
					link.input(inputs.get());
				}
				final ICommand command = link.cmd();
				final Input input = link.input() != null ? new Input(link.input()) : inputs;

				// output pipes redirect the current output value as their input 
				if (command instanceof IOutputPipe<?>) {
					input.set(returns.get());
				}

				final Callable<?> callable = callFactory.create(command, input, returns, called);
				final ExecutorService executor = link.executor();
				if (executor == null) {
					callable.call();
				}
				else {
					executor.submit(callable).get();
				}

				// input pipes redirect their output to the next input value 
				if (command instanceof IInputPipe<?>) {
					inputs.set(returns.get());
				}

				return link.next();
			}


			static class ReturningChain<O>
				extends DefaultChain<O> {

				public ReturningChain(final ILink link) {
					super(link);
				}


				public O invoke(final Object input)
					throws Exception {

					return super.invoke(input);
				}
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
			// returning should be safe, as we are decorating and the decorated chain is responsible for ensuring return safety
			extends ReturningChain<O>
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

				final Input inputs = new Input(input);
				final Called called = new Called();
				final Returns returns = new Returns();
				ILink next = head();
				while (next != null) {
					next = callImpl(next, inputs, returns, called, new DefaultCallFactory(false, true));
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

				return Command.invoke(executing, input, Returns.VOID, Called.nop, true);
			}


			protected ICommand undoImpl(Object input)
				throws Exception {

				return Command.invoke(executing, input, Returns.VOID, Called.nop, true, true);
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
					Chains.builder().add(commands).visits(true).build().invoke(input);
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


	enum Builder {
		/*noinstance*/;

		/**
		 * @see IBaseFluentChainBuilder
		 * @see IChainBuilder
		 * @author wassj
		 *
		 */
		static class BaseBuilder
			implements IChainBuilder {

			private LinkBuilder head;
			private LinkBuilder tail;
			private boolean visits;


			public IReturningChainBuilder<Object> returns() {
				return new ReturningBuilder<Object>(this);
			}


			public <O> IReturningChainBuilder<O> returns(final Class<O> type) {
				return returns(new IFunction<Object, O>() {
					public O invoke(final Object input) {
						return type.isInstance(input) ? type.cast(input) : null;
					}
				});
			}


			public <O> IReturningChainBuilder<O> returns(final IFunction<?, O> function) {
				return new ReturningBuilder<O>(this, function);
			}


			public IChainBuilder visits(final boolean visits) {
				this.visits = visits;
				return this;
			}


			/**
			 * add the {@link ICommand} to the end of the chain
			 * @param command
			 * @return
			 */
			public IChainBuilder add(final ICommand command) {
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
			 * add the {@link Future} to the end of the chain
			 * @param command
			 * @return
			 */
			public <R> IChainBuilder add(final Future<R> future) {
				return add(Concurrent.asCommand(future));
			}


			public IChainBuilder add(final Collection<ICommand> commands) {
				for (final ICommand command : commands) {
					add(command);
				}
				return this;
			}


			/**
			 * init the builder with the passed builder as head (and tail for now)
			 * @param builder
			 * @return
			 */
			private IChainBuilder init(final LinkBuilder builder) {
				this.head = builder;
				this.tail = builder;
				return this;
			}


			/**
			 * set the executor for the tail link
			 * @param executor
			 * @return
			 */
			public IChainBuilder executor(final ExecutorService executor) {
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
			public IChainBuilder input(final Object input) {
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
					return new DefaultChain<Void>(head.build()).callFactory(new DefaultCallFactory(visits, false));
				}
				return new EmptyChain<Void>();
			}
		}


		/**
		 * @see IBaseFluentChainBuilder
		 * @see IReturningChainBuilder
		 * @author wassj
		 * @param <O>
		 */
		static class ReturningBuilder<O>
			implements IReturningChainBuilder<O> {

			private final BaseBuilder base;
			private final IFunction<?, O> returnFunction;


			public ReturningBuilder(final BaseBuilder base) {
				this(base, null);
			}


			public ReturningBuilder(final BaseBuilder base, final IFunction<?, O> returnFunction) {
				this.base = base;
				this.returnFunction = returnFunction;
			}


			public IReturningChainBuilder<O> visits(final boolean visits) {
				base.visits(true);
				return this;
			}


			/**
			 * set the executor for the tail link
			 * @param executor
			 * @return
			 */
			public IReturningChainBuilder<O> executor(final ExecutorService executor) {
				if (base.tail == null) {
					throw new NullPointerException("chain builder was not initialized, tail is null");
				}
				base.tail.executor(executor);
				return this;
			}


			/**
			 * set an individual overriding input for the tail link
			 * @param input
			 * @return
			 */
			public IReturningChainBuilder<O> input(final Object input) {
				if (base.tail == null) {
					throw new NullPointerException("chain builder was not initialized, tail is null");
				}
				base.tail.input(input);
				return this;
			}


			public IReturningChainBuilder<O> add(final ICommand command) {
				base.add(command);
				return this;
			}


			public <R> IReturningChainBuilder<O> add(final Future<R> future) {
				base.add(future);
				return this;
			}


			public IReturningChainBuilder<O> add(final Collection<ICommand> commands) {
				for (final ICommand command : commands) {
					add(command);
				}
				return this;
			}


			public IChain<O> build() {
				if (base.head != null) {
					if (returnFunction != null) {
						/*
						 * Use a visiting chain containing the return function to implement the return behavior.
						 * An IO pipe will transfer the main chain result value into the returning function chain. 
						 * The returning function chain will then be responsible for returning the final result value.
						 * We avoid recursion here because the returning chain can just return type of object.
						 */
						final IChain<Object> returnFunctionChain = Chains.builder().visits(true).add(returnFunction).returns().build();
						base.add(Commands.pipe()).add(returnFunctionChain);
					}
					return new ReturningChain<O>(base.head.build()).callFactory(new DefaultCallFactory(base.visits, false));
				}
				return new EmptyChain<O>();
			}
		}
	}


	/**
	 * {@link Variable} for return values
	 * @author wassj
	 */
	static class Returns
		extends Variable<Object> {

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
		extends Variable<Object> {

		public Input() {
			super();
		}


		public Input(Object value) {
			super(value);
		}
	}


	/**
	 * {@link Variable} for flagging a command as having been run
	 * @author wassj
	 */
	static class Called
		extends Variable<Boolean> {

		public static Called nop = new Called();


		public Called() {
			super(Boolean.FALSE);
		}


		public boolean was() {
			return this.get();
		}


		@Override
		public void set(final Boolean value) {
			if (value == null) {
				throw new IllegalArgumentException("called cannot be null");
			}
			super.set(value);
		}
	}
}
