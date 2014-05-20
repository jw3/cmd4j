package cmd4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
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
import cmd4j.ICommand.IPipe;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.ICommand.IStateCommand;
import cmd4j.ICommand.IStateCommand.IStateCommand1;
import cmd4j.ICommand.IStateCommand.IStateCommand2;
import cmd4j.ICommand.IStateCommand.IStateCommand3;
import cmd4j.ICommand.IStateCommand.IStateCommand4;
import cmd4j.Internals.Chain.DefaultChain;
import cmd4j.Internals.Chain.DefaultChain.ReturningChain;
import cmd4j.Internals.Chain.EmptyChain;
import cmd4j.Internals.Chain.IChainDecorator;
import cmd4j.Internals.Chain.IDecorator;
import cmd4j.Internals.Command.DefaultCallFactory;
import cmd4j.Internals.Command.ICommandCallFactory;
import cmd4j.Internals.Link.LinkBuilder;
import cmd4j.Observers.IObservable;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;

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

		static class CommandRunnable
			implements Runnable {

			private final ICommand command;


			public CommandRunnable(final ICommand command) {
				this.command = command;
			}


			public void run() {
				try {
					Chains.create(command).invoke();
				}
				catch (final Exception e) {
					Throwables.propagate(e);
				}
			}
		}


		static class CommandCallable<R>
			implements Callable<R> {

			private final IReturningCommand<R> command;
			private final Object input;


			public CommandCallable(final IReturningCommand<R> command) {
				this(command, null);
			}


			public CommandCallable(final IReturningCommand<R> command, @Nullable final Object input) {
				this.command = command;
				this.input = input;
			}


			public R call()
				throws Exception {

				return Chains.create(command).invoke(input);
			}
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


			ICommandCallFactory<O> visits(boolean visit);
		}


		/**
		 * default implementation of {@link ICommandCallFactory}
		 * @author wassj
		 */
		static class DefaultCallFactory<O>
			implements ICommandCallFactory<O> {

			private final boolean undo;
			private boolean visit;


			public DefaultCallFactory() {
				this(false, false);
			}


			public DefaultCallFactory(final boolean visit, final boolean undo) {
				this.visit = visit;
				this.undo = undo;
			}


			public ICommandCallFactory<O> visits(final boolean visit) {
				this.visit = visit;
				return this;
			}


			public Callable<O> create(final ICommand head, final Input input, final Returns returns, final Called called) {
				return new Callable<O>() {
					public O call()
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


		/**
		 * {@link ICommandCallFactory} which short circuits after the {@link Predicate} is satisfied
		 * @author wassj
		 * @param <O>
		 */
		static class RunUntilCallFactory<O>
			extends DefaultCallFactory<O> {

			private final Predicate<Object> until;
			private boolean done;


			public RunUntilCallFactory(final Predicate<? extends Object> until) {
				super(true, false);
				this.until = (Predicate<Object>)Preconditions.checkNotNull(until);
			}


			@Override
			public Callable<O> create(final ICommand head, final Input input, final Returns returns, final Called called) {
				return new Callable<O>() {
					public O call()
						throws Exception {

						if (!done) {
							ICommand command = head;
							while (command != null) {
								command = invoke(command, input.get(), returns, called, true, false);

								// dont break on done; this allows state commands to run to completion if they are the one-run
								done = done || satisfied(returns);
							}
						}
						return null;
					}
				};
			}


			private boolean satisfied(final Returns returns) {
				try {
					return until.apply(returns.get());
				}
				catch (final Exception e) {
					// ignore; a mismatch on the predicate and return type is a valid state
					return false;
				}
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
				if (!undo) {
					if (command instanceof IStateCommand4<?, ?>) {
						return ((IStateCommand4)command).invoke(input);
					}
					else if (command instanceof IStateCommand3<?>) {
						return ((IStateCommand3)command).invoke();
					}
					else if (command instanceof IStateCommand2<?>) {
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
					if (command instanceof IStateCommand4.IUndo<?, ?>) {
						return ((IStateCommand4.IUndo)command).undo(input);
					}
					else if (command instanceof IStateCommand3.IUndo<?>) {
						return ((IStateCommand3.IUndo)command).undo();
					}
					else if (command instanceof IStateCommand2.IUndo<?>) {
						return ((IStateCommand2.IUndo)command).undo(input);
					}
					else if (command instanceof IStateCommand1.IUndo) {
						return ((IStateCommand1.IUndo)command).undo();
					}
					else if (command instanceof ICommand4.IUndo<?, ?>) {
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
					final String message = new StringBuilder().append("input [").append(input).append("] ").append("does not fit [").append(command).append("]").toString();
					throw new IllegalArgumentException(message);
				}
				called.set(false);
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

		static ILink build(final LinkBuilder head) {
			final Stack<LinkBuilder> builders = new Stack<LinkBuilder>();

			LinkBuilder current = head;
			while (null != current) {
				builders.push(current);
				current = current.next;
			}

			ILink link = null;
			while (!builders.isEmpty()) {
				final LinkBuilder builder = builders.pop();
				link = new DefaultLink(builder.command, link).executor(builder.executor).input(builder.input);
			}
			return link;
		}


		static ILink empty() {
			return new EmptyLink();
		}


		static ILink create(final ICommand command) {
			return build(new LinkBuilder(command));
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


			public DefaultLink(final ICommand command, @Nullable final ILink next) {
				this.command = command;
				this.next = next;
			}


			public Object input() {
				return input;
			}


			public DefaultLink input(@Nullable final Object input) {
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


			public DefaultLink executor(@Nullable final ExecutorService executor) {
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
			LinkBuilder executor(@Nullable final ExecutorService executor) {
				this.executor = executor;
				return this;
			}


			LinkBuilder add(final ICommand command) {
				next = new LinkBuilder(command);
				return next;
			}


			LinkBuilder input(@Nullable final Object input) {
				this.input = input;
				return this;
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


			public ILink input(@Nullable final Object input) {
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
		 * @author wassj
		 */
		static class EmptyChain<O>
			extends DefaultChain<O> {

			public EmptyChain() {
				super(Link.empty());
			}
		}


		/**
		 * the default {@link IChain} implementation
		 * @author wassj
		 */
		static class DefaultChain<O>
			implements IChain<O>, IPeekable {

			private final Returns returns = new Returns();
			private final ILink link;
			private boolean visits;

			private ICommandCallFactory<?> callFactory = new DefaultCallFactory<O>();


			DefaultChain(final ILink link) {
				this.link = link;
			}


			public DefaultChain<O> visits(final boolean visits) {
				this.visits = visits;
				return this;
			}


			public boolean visits() {
				return visits;
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


			public O invoke(@Nullable final Object input)
				throws Exception {

				final Input inputs = new Input(input);
				final Called called = new Called();
				ILink next = head();
				while (next != null) {
					next = callImpl(next, inputs, returns, called, callFactory);
				}
				return null;//(O)returns();
			}


			protected ILink callImpl(final ILink link, final Input inputs, final Returns returns, final Called called, final ICommandCallFactory<?> callFactory)
				throws Exception {

				Object linkDotInput = link.input();
				if (!inputs.isNull() && linkDotInput == null) {
					linkDotInput = inputs.get();
				}
				final Input input = linkDotInput != null ? new Input(linkDotInput) : inputs;
				final ICommand command = link.cmd();

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


				public O invoke(@Nullable final Object input)
					throws Exception {

					super.invoke(input);
					return (O)returns();
				}
			}


			public ILink head() {
				return link;
			}
		}


		/**
		 * {@link Callable} wrapper for a {@link IChain}
		 * @author wassj
		 * @param <O>
		 */
		static class ChainCallable<O>
			implements Callable<O> {

			private final IChain<O> chain;
			private final Object input;


			public ChainCallable(final IChain<O> chain) {
				this(chain, null);
			}


			public ChainCallable(final IChain<O> chain, @Nullable final Object input) {
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


		static class VisitingChainDecorator<O>
			extends ReturningChain<O>
			implements IChainDecorator<O> {

			private final IChain<O> chain;


			public VisitingChainDecorator(final IChain<O> chain) {
				super(chain.head());
				this.chain = chain;
			}


			public IChain<O> decorating() {
				return chain;
			}


			public ILink head() {
				return chain.head();
			}


			protected ILink callImpl(final ILink link, final Input inputs, final Returns returns, final Called called, final ICommandCallFactory<?> callFactory)
				throws Exception {

				return super.callImpl(link, inputs, returns, called, callFactory.visits(true));
			}
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


			public O undo(@Nullable final Object input)
				throws Exception {

				final Input inputs = new Input(input);
				final Called called = new Called();
				final Returns returns = new Returns();
				boolean visits = this.visits();
				ILink next = head();
				while (next != null) {
					if (next.cmd() instanceof IChain<?>) {
						final IChain<?> chain = (IChain<?>)next.cmd();
						next = Chains.undoable(chain).head();
						visits = chain.visits();
					}
					next = callImpl(next, inputs, returns, called, new DefaultCallFactory<O>(visits, true));
				}
				return (O)returns.get();
			}
		}


		/**
		 * test a chain for peeking capability
		 * @param chain
		 * @return a peekable
		 * @Beta
		 */
		static IPeekable assertPeekable(final IChain<?> chain) {
			Preconditions.checkArgument(chain instanceof IPeekable, "unsupported chain impl, not peekable");
			return (IPeekable)chain;
		}


		/**
		 * somewhat of a hack to work around the returning Void issue
		 * @author wassj
		 * @Beta
		 */
		interface IPeekable {
			Object returns();
		}


		/**
		 * somewhat of a hack to work around the returning Void issue
		 * @see IPeekable
		 * @author wassj
		 * @Beta
		 */
		static ICommand peekAt(final IChain<?> chain) {
			return new ICommand3<Object>() {
				public Object invoke() {
					return assertPeekable(chain).returns();
				}
			};
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


			protected ICommand invokeImpl(@Nullable final Object input)
				throws Exception {

				return Command.invoke(executing, input, Returns.VOID, Called.nop, true);
			}


			protected ICommand undoImpl(@Nullable final Object input)
				throws Exception {

				return Command.invoke(executing, input, Returns.VOID, Called.nop, true, true);
			}


			@Override
			public ICommand invoke(@Nullable final Object input)
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


			protected O invokeImpl(@Nullable final Object input)
				throws Exception {

				return Chains.create(this.decorating()).invoke(input);
			}


			protected O undoImpl(@Nullable final Object input)
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


			public boolean visits() {
				return this.decorating().visits();
			}


			public ILink head() {
				return this.decorating().head();
			}


			protected O invokeImpl(@Nullable final Object input)
				throws Exception {

				this.decorating().invoke(input);
				return (O)Chain.assertPeekable(this.decorating()).returns();
			}


			protected O undoImpl(@Nullable final Object input)
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


			public O invoke(@Nullable final Object input)
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


			public O undo(@Nullable final Object input)
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


			protected void executeHandlers(final List<ICommand> commands, @Nullable final Object input) {
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
			private boolean pipeAll;


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


			public <O> IReturningChainBuilder<O> returns(final IReturningCommand<O> command) {
				return new ReturningBuilder<O>(this, command);
			}


			public IChainBuilder visits(final boolean visits) {
				this.visits = visits;
				return this;
			}


			public IChainBuilder pipeAll(final boolean pipeAll) {
				this.pipeAll = pipeAll;
				return this;
			}


			/**
			 * add the {@link ICommand} to the end of the chain
			 * @param command
			 * @return
			 */
			public IChainBuilder add(final ICommand command) {
				Preconditions.checkNotNull(command, "command cannot be null");

				// if created with the noarg create() method it will need initd on the first add
				if (head == null) {
					this.init(new LinkBuilder(command));
				}
				else {
					tail = tail != null ? tail.add(command) : new LinkBuilder(command);
				}

				if (pipeAll) {
					tail = tail.add(Commands.pipe());
				}

				return this;
			}


			/**
			 * add the {@link Future} to the end of the chain
			 * @param command
			 * @return
			 */
			public <R> IChainBuilder add(final Future<R> future) {
				return add(Commands.from(future));
			}


			public IChainBuilder add(final Collection<? extends ICommand> commands) {
				for (final ICommand command : commands) {
					add(command);
				}
				return this;
			}


			public IChainBuilder pipe() {
				return add(Commands.pipe());
			}


			public IChainBuilder pipe(final IPipe pipe) {
				Preconditions.checkNotNull(pipe);
				return add(pipe);
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
			public IChainBuilder executor(@Nullable final ExecutorService executor) {
				Preconditions.checkNotNull(tail, "chain builder was not initialized, tail is null");
				tail.executor(executor);
				return this;
			}


			/**
			 * set an individual overriding input for the tail link
			 * @param input
			 * @return
			 */
			public IChainBuilder input(@Nullable final Object input) {
				Preconditions.checkNotNull(tail, "chain builder was not initialized, tail is null");
				tail.input(input);
				return this;
			}


			public IChain<Void> build() {
				return get();
			}


			/**
			 * construct an {@link IChain} object from the {@link ICommand}s that have been added to this builder
			 * @return
			 */
			public IChain<Void> get() {
				return build(new DefaultCallFactory<Void>(visits, false));
			}


			public IChain<Void> build(final ICommandCallFactory<Void> callFactory) {
				if (head != null) {
					return new DefaultChain<Void>(Link.build(head)).callFactory(callFactory).visits(visits);
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
			private final IReturningCommand<O> returnCommand;


			public ReturningBuilder(final BaseBuilder base) {
				this(base, null);
			}


			public ReturningBuilder(final BaseBuilder base, @Nullable final IReturningCommand<O> returnCommand) {
				this.base = base;
				this.returnCommand = returnCommand;
			}


			public IReturningChainBuilder<O> visits(final boolean visits) {
				base.visits(true);
				return this;
			}


			public IReturningChainBuilder<O> pipeAll(final boolean pipeAll) {
				base.pipeAll(pipeAll);
				return this;
			}


			/**
			 * set the executor for the tail link
			 * @param executor
			 * @return
			 */
			public IReturningChainBuilder<O> executor(@Nullable final ExecutorService executor) {
				Preconditions.checkNotNull(base.tail, "chain builder was not initialized, tail is null");
				base.tail.executor(executor);
				return this;
			}


			/**
			 * set an individual overriding input for the tail link
			 * @param input
			 * @return
			 */
			public IReturningChainBuilder<O> input(@Nullable final Object input) {
				Preconditions.checkNotNull(base.tail, "chain builder was not initialized, tail is null");
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


			public IReturningChainBuilder<O> add(final Collection<? extends ICommand> commands) {
				for (final ICommand command : commands) {
					add(command);
				}
				return this;
			}


			public IReturningChainBuilder<O> pipe() {
				return add(Commands.pipe());
			}


			public IReturningChainBuilder<O> pipe(final IPipe pipe) {
				Preconditions.checkNotNull(pipe);
				return add(pipe);
			}


			public IChain<O> build() {
				return get();
			}


			public IChain<O> get() {
				return build(new DefaultCallFactory<O>(base.visits, false));
			}


			public IChain<O> build(final ICommandCallFactory<O> callFactory) {
				if (base.head != null) {
					if (returnCommand != null) {
						/*
						 * Use a visiting chain containing the return function to implement the return behavior.
						 * An IO pipe will transfer the main chain result value into the returning function chain. 
						 * The returning function chain will then be responsible for returning the final result value.
						 * We avoid recursion here because the returning chain can just return type of object.
						 */
						final IChain<Object> returnFunctionChain = Chains.builder().visits(true).add(returnCommand).returns().build();
						base.add(Commands.pipe()).add(returnFunctionChain);
					}
					return new ReturningChain<O>(Link.build(base.head)).callFactory(callFactory);
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
			public void set(@Nullable final Object value) {
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


		public Input(@Nullable final Object value) {
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
			Preconditions.checkNotNull(value, "called cannot be null");
			super.set(value);
		}
	}
}
