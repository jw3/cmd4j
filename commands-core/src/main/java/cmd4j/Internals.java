package cmd4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cmd4j.Chains.ChainBuilder;
import cmd4j.IChain.IObservableChain;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.ICommand5;
import cmd4j.ICommand.ICommand6;
import cmd4j.ICommand.IObservableCommand;
import cmd4j.ICommand.IObservableStateCommand;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.ICommand.IStateCommand;
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
			private final Object dto;


			public CommandCallable(final IReturningCommand<R> command) {
				this(command, null);
			}


			public CommandCallable(final IReturningCommand<R> command, final Object dto) {
				this.command = command;
				this.dto = dto;
			}


			public R call()
				throws Exception {

				return Chains.returning(command).invoke(dto);
			}
		}


		/**
		 * wrap a command to ensure a Void return type
		 * useful for:
		 * 1. ensuring return type is ignored
		 * 2. adding return type to a non returner
		 *
		 * @author wassj
		 */
		static class ReturnVoidWrapper
			implements ICommand4<Object, Void> {

			private final ICommand command;


			public ReturnVoidWrapper(final ICommand command) {
				this.command = command;
			}


			public Void invoke(final Object dto)
				throws Exception {

				Link.invokeCommand(command, dto, true);
				return null;
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

			IDtoCommand<C> command();
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

			private final ICommand command;
			private final ILink next;

			private Object dto;
			private ExecutorService executor;
			private boolean ignoreDtoMismatch;


			public DefaultLink(final ICommand command, final ILink next) {
				this.command = command;
				this.next = next;
			}


			public Object dto() {
				return dto;
			}


			public DefaultLink dto(final Object dto) {
				this.dto = dto;
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


			public DefaultLink ignoreDtoMismatch(final boolean ignoreDtoMismatch) {
				this.ignoreDtoMismatch = ignoreDtoMismatch;
				return this;
			}


			/**
			 * Execute the {@link ICommand} and return the next {@link ILink}
			 */
			public ILink call()
				throws Exception {

				//		try {
				ICommand command = cmd();
				while (command != null) {
					// REVISIT not sure what the following is doing?
					final Object dto = dto() != null ? dto() : this.dto;
					final Object returned = invokeCommand(command, dto, ignoreDtoMismatch);
					command = command instanceof IStateCommand && returned instanceof ICommand ? (ICommand)returned : null;
				}
				return next();
				//		}
				//		catch (Exception e) {
				//			if (failsafe) {
				//				return next();
				//			}
				//			throw e;
				//		}
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
			private Object dto;


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


			LinkBuilder add(final LinkBuilder builder) {
				next = builder;
				return builder.next;
			}


			LinkBuilder dto(final Object dto) {
				this.dto = dto;
				return this;
			}


			ILink build() {
				return new DefaultLink(command, next != null ? next.build() : null).executor(executor).dto(dto);
			}


			ILink build(boolean visits) {
				return new DefaultLink(command, next != null ? next.build(visits) : null).executor(executor).dto(dto).ignoreDtoMismatch(visits);
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


			public Object dto() {
				return null;
			}


			public ILink dto(Object dto) {
				return this;
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


		// REVISIT should this support dto mismatch ignores?
		static class LinkUndoDecorator
			implements ILink {

			private final ILink link;


			public LinkUndoDecorator(final ILink link) {
				this.link = link;
			}


			public ILink next() {
				return link.next();
			}


			public Object dto() {
				return link.dto();
			}


			public ILink dto(Object dto) {
				return link.dto(dto);
			}


			public ICommand cmd() {
				return link.cmd();
			}


			public ExecutorService executor() {
				return link.executor();
			}


			@Override
			public ILink call()
				throws Exception {

				//		try {
				/*
				 * REVISIT lots of duplication with DefaultLink now, perhaps these can be rolled together?
				 */
				ICommand command = cmd();
				while (command != null) {
					// REVISIT not sure what the following is doing? 
					final Object dto = dto() != null ? dto() : this.dto();
					final Object returned = invokeCommand(command, dto, false, true);
					command = command instanceof IStateCommand && returned instanceof ICommand ? (ICommand)returned : null;
				}
				return next();
				//		}
				//		catch (Exception e) {
				//			if (failsafe) {
				//				return next();
				//			}
				//			throw e;
				//		}
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
		 * @param dto
		 * @return
		 * @throws Exception
		 */
		static Object invokeCommand(final ICommand command, final Object dto, final boolean ignoreDtoMismatch)
			throws Exception {

			return invokeCommand(command, dto, ignoreDtoMismatch, false);
		}


		@SuppressWarnings({"unchecked", "rawtypes"})
		/// safely suppressed here: we do some extra checking to ensure the dto fits in the invocation
		static Object invokeCommand(final ICommand command, final Object dto, final boolean ignoreDtoMismatch, final boolean undo)
			throws Exception {

			try {
				if (command instanceof ICommandProxy) {
					return ((ICommandProxy)command).command();
				}
				else if (!undo) {
					if (command instanceof ICommand5) {
						return ((ICommand5)command).invoke();
					}
					else if (command instanceof ICommand6<?>) {
						return ((ICommand6)command).invoke(dto);
					}
					else if (command instanceof ICommand4<?, ?>) {
						return ((ICommand4)command).invoke(dto);
					}
					else if (command instanceof ICommand2<?>) {
						((ICommand2)command).invoke(dto);
					}
					else if (command instanceof ICommand3<?>) {
						return ((ICommand3)command).invoke();
					}
					else if (command instanceof ICommand1) {
						((ICommand1)command).invoke();
					}
				}
				else {
					if (command instanceof ICommand4.IUndo<?, ?>) {
						return ((ICommand4.IUndo)command).undo(dto);
					}
					else if (command instanceof ICommand2.IUndo<?>) {
						((ICommand2.IUndo)command).undo(dto);
					}
					else if (command instanceof ICommand3.IUndo<?>) {
						return ((ICommand3.IUndo)command).undo();
					}
					else if (command instanceof ICommand1.IUndo) {
						((ICommand1.IUndo)command).undo();
					}
				}
			}
			catch (final ClassCastException e) {
				if (!ignoreDtoMismatch) {
					throw new IllegalArgumentException("dto does not fit");
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
		static class EmptyChain
			implements IChain<Void> {

			private final ILink head = Link.empty();


			public ILink head() {
				return head;
			}


			public Void invoke() {
				return null;
			}


			public Void invoke(final Object dto) {
				return null;
			}
		}


		/**
		 * the default {@link IChain} implementation
		 *
		 * @author wassj
		 */
		static class DefaultChain<R>
			implements IChain<R> {

			private final ILink link;
			private final ICommand command;
			private final IReturningCommand<R> returningCommand;


			public DefaultChain(final ILink link) {
				this(link, null, null);
			}


			public DefaultChain(final ICommand command) {
				this(null, command, null);
			}


			public DefaultChain(final IReturningCommand<R> returningCommand) {
				this(null, null, returningCommand);
			}


			private DefaultChain(final ILink link, final ICommand command, final IReturningCommand<R> returningCommand) {
				this.link = link;
				this.command = command;
				this.returningCommand = returningCommand;
			}


			public R invoke()
				throws Exception {

				return this.invoke(null);
			}


			public R invoke(Object dto)
				throws Exception {

				if (link != null) {
					final Linker linker = new Linker(this.head(), dto);
					Concurrent.sameThreadExecutor().submit(linker).get();
					return null;
				}
				if (returningCommand != null) {
					@SuppressWarnings("unchecked")
					//should be safe cast here
					final R retval = (R)Internals.Link.invokeCommand(returningCommand, dto, false);
					return retval;
				}

				Internals.Link.invokeCommand(command, dto, false);

				return null;
			}


			public ILink head() {
				return link;
			}
		}


		static class ChainCallable<O>
			implements Callable<O> {

			private final IChain<O> chain;
			private final Object dto;


			public ChainCallable(final IChain<O> chain) {
				this(chain, null);
			}


			public ChainCallable(final IChain<O> chain, final Object dto) {
				this.chain = chain;
				this.dto = dto;
			}


			public O call()
				throws Exception {

				return chain.invoke(dto);
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
		static class Linker
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


			protected ILink callImpl(final ILink link)
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
		static class UndoLinker
			extends Linker {

			public UndoLinker(final ILink head, final Object dto) {
				super(head, dto);
			}


			public Void call()
				throws Exception {

				ILink next = head();
				while (next != null) {
					next = Link.undo(next);
					next = callImpl(next);
				}
				return null;
			}
		}


		/**
		 *
		 * @author wassj
		 * @param <T>
		 */
		interface IDecorator<T> {
			T getDecorating();
		}


		/**
		 *
		 * @author wassj
		 */
		interface IChainDecorator<R>
			extends IDecorator<IChain<R>>, IChain<R> {

			IChain<R> getDecorating();
		}


		/**
		 *
		 * @author wassj
		 */
		static class UndoableChainDecorator<R>
			implements IChainDecorator<R> {

			private final IChain<R> chain;


			public UndoableChainDecorator(final IChain<R> chain) {
				this.chain = chain;
			}


			public IChain<R> getDecorating() {
				return chain;
			}


			public ILink head() {
				return chain.head();
			}


			public R invoke()
				throws Exception {

				return this.invoke(null);
			}


			public R invoke(final Object dto)
				throws Exception {

				final Linker linker = new UndoLinker(this.head(), dto);
				Concurrent.sameThreadExecutor().submit(linker).get();
				return null;
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
		 * cast to or create an {@link IObservableCommand} from the passed {@link ICommand} instance
		 * @param command
		 * @return
		 */
		static <O> IObservableCommand<O> observerDecorator(final IReturningCommand<O> command) {
			return command instanceof IObservableCommand<?> ? (IObservableCommand<O>)command : new ObservableCommandDecorator<O>(command);
		}


		/**
		 * cast to or create an {@link IObservableChain} from the passed {@link IChain} instance
		 * @param chain
		 * @return
		 */
		static <O> IObservableChain<O> observerDecorator(final IChain<O> chain) {
			return chain instanceof IObservableChain<?> ? (IObservableChain<O>)chain : new ObservableChainDecorator<O>(chain);
		}


		static IObservableStateCommand observerDecorator(final IStateCommand command) {
			return command instanceof IObservableStateCommand ? (IObservableStateCommand)command : new StateCommandDecorator(command);
		}


		/**
		 *
		 * @author wassj
		 */
		static class StateCommandDecorator
			extends AbstractObservable<ICommand, IObservableStateCommand, IStateCommand>
			implements IObservableStateCommand, IStateCommand, ICommand6<Object> {

			private ICommand executing;


			public StateCommandDecorator(final IStateCommand command) {
				super(command);
			}


			protected ICommand invokeImpl(final Object dto)
				throws Exception {

				final Object returned = Link.invokeCommand(executing, dto, true);
				return /*command instanceof IStateCommand &&*/returned instanceof ICommand ? (ICommand)returned : null;
			}


			@Override
			public ICommand invoke(final Object dto)
				throws Exception {

				executing = this.getDecorating();
				while (executing != null) {
					try {
						executeHandlers(beforeHandlers(), dto);
						executing = invokeImpl(dto);
						///executeHandlers(resultsHandlers(), returned);
						executeHandlers(successHandlers(), dto);
					}
					catch (ExecutionException e) {
						executeHandlers(failureHandlers(), e.getCause());
						throw e;
					}
					catch (Exception e) {
						executeHandlers(failureHandlers(), e);
						throw e;
					}
					finally {
						executeHandlers(afterHandlers(), dto);
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


			protected O invokeImpl(final Object dto)
				throws Exception {

				return Chains.returning(this.getDecorating()).invoke(dto);
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
				return this.getDecorating().head();
			}


			protected O invokeImpl(final Object dto)
				throws Exception {

				return this.getDecorating().invoke(dto);
			}
		}


		/**
		 * abstract superclass of the {@link IObservable} implementations contains all the core logic;
		 * the subclasses exist to provide a distinction between {@link IChain} and {@link ICommand} observables
		 * 
		 * @author wassj
		 *
		 * @param <O> the return type
		 * @param <T> the IObservable type
		 * @param <C> the ICommand type
		 */
		@SuppressWarnings("unchecked")
		/// Explain: there are some crazy generic parms here, resulting from the IObservable interface being typed with a generic form of itself
		abstract static class AbstractObservable<O, T extends IObservable<?>, C extends ICommand>
			implements IObservable<T>, IDecorator<C> {

			private final List<ICommand> afterHandlers = new LinkedList<ICommand>();
			private final List<ICommand> beforeHandlers = new LinkedList<ICommand>();
			private final List<ICommand> resultsHandlers = new LinkedList<ICommand>();
			private final List<ICommand> successHandlers = new LinkedList<ICommand>();
			private final List<ICommand> failureHandlers = new LinkedList<ICommand>();

			private final C command;


			public AbstractObservable(final C command) {
				this.command = command;
			}


			public C getDecorating() {
				return command;
			}


			abstract protected O invokeImpl(final Object dto)
				throws Exception;


			public O invoke()
				throws Exception {

				return this.invoke(null);
			}


			public O invoke(final Object dto)
				throws Exception {

				try {
					final O returned;
					executeHandlers(beforeHandlers(), dto);
					returned = invokeImpl(dto);
					executeHandlers(resultsHandlers(), returned);
					executeHandlers(successHandlers(), dto);
					return returned;
				}
				catch (ExecutionException e) {
					executeHandlers(failureHandlers(), e.getCause());
					throw e;
				}
				catch (Exception e) {
					executeHandlers(failureHandlers(), e);
					throw e;
				}
				finally {
					executeHandlers(afterHandlers(), dto);
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


			public T results(ICommand... commands) {
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


			List<ICommand> afterHandlers() {
				return afterHandlers;
			}


			List<ICommand> beforeHandlers() {
				return beforeHandlers;
			}


			List<ICommand> resultsHandlers() {
				return resultsHandlers;
			}


			List<ICommand> successHandlers() {
				return successHandlers;
			}


			List<ICommand> failureHandlers() {
				return failureHandlers;
			}


			protected void executeHandlers(final List<ICommand> commands, final Object dto) {
				try {
					Chains.create(commands).invoke(dto);
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


		/*
		 * taken from Guava SameThreadExecutorService
		 */
		static class SameThreadExecutorService
			extends AbstractExecutorService {

			/**
			 * Lock used whenever accessing the state variables
			 * (runningTasks, shutdown, terminationCondition) of the executor
			 */
			private final Lock lock = new ReentrantLock();

			/** Signaled after the executor is shutdown and running tasks are done */
			private final Condition termination = lock.newCondition();

			/*
			 * Conceptually, these two variables describe the executor being in
			 * one of three states:
			 *   - Active: shutdown == false
			 *   - Shutdown: runningTasks > 0 and shutdown == true
			 *   - Terminated: runningTasks == 0 and shutdown == true
			 */
			private int runningTasks = 0;
			private boolean shutdown = false;


			@Override
			public void execute(Runnable command) {
				startTask();
				try {
					command.run();
				}
				finally {
					endTask();
				}
			}


			@Override
			public boolean isShutdown() {
				lock.lock();
				try {
					return shutdown;
				}
				finally {
					lock.unlock();
				}
			}


			@Override
			public void shutdown() {
				lock.lock();
				try {
					shutdown = true;
				}
				finally {
					lock.unlock();
				}
			}


			// See sameThreadExecutor javadoc for unusual behavior of this method.
			@Override
			public List<Runnable> shutdownNow() {
				shutdown();
				return Collections.emptyList();
			}


			@Override
			public boolean isTerminated() {
				lock.lock();
				try {
					return shutdown && runningTasks == 0;
				}
				finally {
					lock.unlock();
				}
			}


			@Override
			public boolean awaitTermination(long timeout, TimeUnit unit)
				throws InterruptedException {
				long nanos = unit.toNanos(timeout);
				lock.lock();
				try {
					for (;;) {
						if (isTerminated()) {
							return true;
						}
						else if (nanos <= 0) {
							return false;
						}
						else {
							nanos = termination.awaitNanos(nanos);
						}
					}
				}
				finally {
					lock.unlock();
				}
			}


			/**
			 * Checks if the executor has been shut down and increments the running
			 * task count.
			 *
			 * @throws RejectedExecutionException if the executor has been previously
			 *         shutdown
			 */
			private void startTask() {
				lock.lock();
				try {
					if (isShutdown()) {
						throw new RejectedExecutionException("Executor already shutdown");
					}
					runningTasks++;
				}
				finally {
					lock.unlock();
				}
			}


			/**
			 * Decrements the running task count.
			 */
			private void endTask() {
				lock.lock();
				try {
					runningTasks--;
					if (isTerminated()) {
						termination.signalAll();
					}
				}
				finally {
					lock.unlock();
				}
			}
		}
	}
}
