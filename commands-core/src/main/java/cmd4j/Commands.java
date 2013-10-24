package cmd4j;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.IDtoCommand;
import cmd4j.ICommand.IObservableCommand;

/**
 * Utility methods for {@link ICommand commands}
 *
 * @author wassj
 *
 */
public enum Commands {
	/*singleton-enum*/;
	/**
	 * execute the specified {@link ICommand command}
	 * @throws Exception
	 */
	public static void execute(final ICommand command)
		throws Exception {

		Chains.builder().add(command).build().invoke();
	}


	public static IObservableCommand observable(final ICommand command) {
		return decorator(command);
	}


	/**
	 * {@link ICommand command} that will wait for a specified number of {@link TimeUnit#MILLISECONDS milliseconds}
	 * @param timeout
	 * @return
	 */
	public static ICommand waitFor(final long timeout) {
		return waitFor(timeout, TimeUnit.MILLISECONDS);
	}


	/**
	 * {@link ICommand command} that will wait for a specified number of specified {@link TimeUnit time unit}
	 * @param timeout
	 * @return
	 */
	public static ICommand waitFor(final long timeout, final TimeUnit unit) {
		return new ICommand1() {
			public void invoke()
				throws Exception {

				new CountDownLatch(1).await(timeout, unit);
			}
		};
	}


	/**
	 * wait on the latch to open
	 * @param latch
	 * @return Command that is blocked until latch is released
	 */
	public static ICommand waitFor(final CountDownLatch latch) {
		return new ICommand1() {
			public void invoke()
				throws Exception {

				latch.await();
			}
		};
	}


	/**
	 * count down the latch
	 * @param latch
	 * @return
	 */
	public static ICommand countDown(final CountDownLatch latch) {
		return new ICommand1() {
			public void invoke()
				throws Exception {
				latch.countDown();
			}
		};
	}


	/**
	 * create an empty (no-operation) {@link ICommand command}
	 * @return Command that does nothing
	 */
	public static ICommand nop() {
		return new ICommand1() {
			public void invoke() {
			}
		};
	}


	/**
	 * wrap a future in a command
	 * @param future
	 * @return
	 */
	public static ICommand future(final Future<?> future) {
		return new ICommand1() {
			public void invoke()
				throws Exception {
				future.get();
			}
		};
	}


	/**
	 * wrap a callable in a command
	 * @param future
	 * @return
	 */
	public static ICommand callable(final Callable<?> callable) {
		return new ICommand1() {
			public void invoke()
				throws Exception {
				callable.call();
			}
		};
	}


	/**
	 * tokenize the type parameter of the command
	 * useful only when erasure is removing necessary type information
	 * @param type
	 * @param command
	 * @return
	 */
	public static <T> ICommand tokenize(Class<T> type, IDtoCommand<T> command) {
		return new DtoTokenizerProxy<T>(command, type);
	}


	public interface ICommandProxy<C extends ICommand>
		extends ICommand {

		IDtoCommand<C> command();
	}


	/**
	 * Provide the means of storing the acceptable dto type on a command, useful as a workaround to erasure.
	 * Provide callback capability
	 *
	 * @author wassj
	 */
	public interface ITokenized<T>
		extends ICommandProxy {

		Class<T> dtoType();
	}


	private static class DtoTokenizerProxy<T>
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


	private static IObservableCommand decorator(final ICommand chain) {
		return chain instanceof IObservableCommand ? (IObservableCommand)chain : new ObservableCommandDecorator(chain);
	}


	/**
	 *
	 * @author wassj
	 */
	public static class ObservableCommandDecorator
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
