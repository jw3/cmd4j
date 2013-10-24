package cmd4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import cmd4j.Chains.ChainBuilder;
import cmd4j.IChain.IReturningChain;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IObservableCommand;
import cmd4j.ICommand.IUndo;
import cmd4j.Internals.Command.ICommandProxy;
import cmd4j.Internals.Command.ITokenized;

/**
 *
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

		interface ICommandProxy<C extends ICommand>
			extends ICommand {

			IDtoCommand<C> command();
		}


		/**
		 * Provide the means of storing the acceptable dto type on a command, useful as a workaround to erasure.
		 * Provide callback capability
		 *
		 * @author wassj
		 */
		interface ITokenized<T>
			extends ICommandProxy {

			Class<T> dtoType();
		}


		static class DtoTokenizerProxy<T>
			implements ITokenized<T> {

			private final IDtoCommand<T> command;
			private final Class<T> type;


			public DtoTokenizerProxy(final IDtoCommand<T> command, final Class<T> type) {
				this.command = command;
				this.type = type;
			}


			public Class<T> dtoType() {
				return type;
			}


			public IDtoCommand<T> command() {
				return command;
			}
		}


		static IObservableCommand decorator(final ICommand chain) {
			return chain instanceof IObservableCommand ? (IObservableCommand)chain : new ObservableCommandDecorator(chain);
		}


		/**
		 *
		 * @author wassj
		 */
		static class ObservableCommandDecorator
			implements IObservableCommand, ICommand2<Object> {

			private final List<ICommand> afterHandlers = new LinkedList<ICommand>();
			private final List<ICommand> beforeHandlers = new LinkedList<ICommand>();
			private final List<ICommand> resultsHandlers = new LinkedList<ICommand>();
			private final List<ICommand> successHandlers = new LinkedList<ICommand>();
			private final List<ICommand> failureHandlers = new LinkedList<ICommand>();

			private final ICommand command;


			public ObservableCommandDecorator(final ICommand command) {
				this.command = command;
			}


			public ICommand command() {
				return command;
			}


			public IObservableCommand before(final ICommand... commands) {
				beforeHandlers.addAll(Arrays.asList(commands));
				return this;
			}


			public IObservableCommand after(final ICommand... commands) {
				afterHandlers.addAll(Arrays.asList(commands));
				return this;
			}


			public IObservableCommand results(ICommand... commands) {
				resultsHandlers.addAll(Arrays.asList(commands));
				return this;
			}


			public IObservableCommand onSuccess(final ICommand... commands) {
				successHandlers.addAll(Arrays.asList(commands));
				return this;
			}


			public IObservableCommand onFailure(final ICommand... commands) {
				failureHandlers.addAll(Arrays.asList(commands));
				return this;
			}


			/**
			 * i am only calling the result handlers when the command was a {@link IReturningCommand}
			 * not convinced on the semantics of this at this point... perhaps the success handler should
			 * be called; but then what do you do about dto vs returned?  calling the success handlers 2x,
			 * once with each of those could be an answer.  nothing really stands out now as the best choice
			 * though that could be because it is 0041 and there is no coffee left... 
			 */
			public void invoke(final Object dto)
				throws Exception {

				try {
					executeHandlers(beforeHandlers, dto);
					if (command instanceof IReturningCommand<?>) {
						final Object returned = Chains.create((IReturningCommand<?>)command).invoke(dto);
						executeHandlers(resultsHandlers, returned);
					}
					else {
						Chains.create(command).invoke(dto);
					}
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
					final Object dto = dto() != null ? dto() : this.dto;
					final Object returned = invokeCommand(command, dto, ignoreDtoMismatch);
					command = returned instanceof ICommand ? (ICommand)returned : null;
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
				return Commands.nop();
			}


			public ExecutorService executor() {
				return null;
			}


			public ILink call() {
				return next();
			}
		}


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
				ICommand command = cmd();
				if (command instanceof IUndo) {
					((IUndo)command).undo();
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
		@SuppressWarnings("unchecked")
		/// safely suppressed here: we do some extra checking to ensure the dto fits in the invocation
		static Object invokeCommand(final ICommand command, final Object dto, final boolean ignoreDtoMismatch)
			throws Exception {

			// REVISIT will have to check the proxy out prior to the castable call in some cases
			final boolean castable = dtoIsCastableForCommand(command, dto);
			if (castable) {
				if (command instanceof ICommand4<?, ?>) {
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
				else if (command instanceof ICommandProxy) {
					return ((ICommandProxy)command).command();
				}
			}
			else if (!ignoreDtoMismatch) {
				throw new IllegalArgumentException("dto does not fit");
			}
			return null;
		}


		/**
		 * check that the passed dto fits into the passed ICommand#invoke method.
		 * @param command
		 * @param dto
		 * @return
		 */
		static boolean dtoIsCastableForCommand(final ICommand command, final Object dto) {
			if (dto != null) {
				final Class<?> cmdType = command instanceof ITokenized<?> ? ((ITokenized<?>)command).dtoType() : typedAs(command);
				final Class<?> dtoType = dto.getClass();
				return cmdType.isAssignableFrom(dtoType);
			}
			return true;
		}


		/**
		 * get the type parameter; if any
		 */
		static Class<?> typedAs(final Object object) {
			for (Type type : object.getClass().getGenericInterfaces()) {
				if (type instanceof ParameterizedType) {
					final Type[] args = ((ParameterizedType)type).getActualTypeArguments();
					if (args.length > 0 && args[0] instanceof Class<?>) {
						return (Class<?>)args[0];
					}
				}
			}
			return Object.class;
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
		static class DefaultChain
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


		/**
		 * the default {@link IChain} implementation
		 *
		 * @author wassj
		 */
		static class ReturningChain<R>
			implements IReturningChain<R> {

			private final IReturningCommand<R> command;


			public ReturningChain(final IReturningCommand<R> command) {
				this.command = command;
			}


			public R invoke()
				throws Exception {

				return this.invoke(null);
			}


			public R invoke(Object dto)
				throws Exception {

				@SuppressWarnings("unchecked")
				// this should be safe, the class param guards it
				final R returned = (R)Internals.Link.invokeCommand(command, dto, false);
				return returned;
			}
		}


		static class ChainCallable<D, R>
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
					next = Links.undo(next);
					next = callImpl(next);
				}
				return null;
			}
		}


		/**
		 *
		 * @author wassj
		 */
		interface IChainDecorator
			extends IChain {

			IChain getDecorating();
		}


		/**
		 *
		 * @author wassj
		 */
		static class UndoableChainDecorator
			implements IChainDecorator {

			private final IChain chain;


			public UndoableChainDecorator(final IChain chain) {
				this.chain = chain;
			}


			public IChain getDecorating() {
				return chain;
			}


			public ILink head() {
				return chain.head();
			}


			public void invoke()
				throws Exception {

				this.invoke(null);
			}


			public void invoke(final Object dto)
				throws Exception {

				final Linker linker = new UndoLinker(this.head(), dto);
				Executors2.sameThreadExecutor().submit(linker).get();
			}
		}
	}
}
