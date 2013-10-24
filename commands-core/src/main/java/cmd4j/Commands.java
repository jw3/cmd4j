package cmd4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommandCallback;
import cmd4j.ICommand.IReturningCommand;

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
	public static <T> ICommand tokenize(Class<T> type, ICommand2<T> command) {
		return new DtoTokenizerProxy<T>(command, type);
	}


	/**
	 * tokenize the type parameter of the command
	 * useful only when erasure is removing necessary type information
	 * @param type
	 * @param command
	 * @return
	 */
	public static <T> ICommand tokenize(Class<T> type, ICommand3<T> command) {
		if (command instanceof ITokenized<?>) {
			throw new IllegalArgumentException("command was already tokenized");
		}
		return new DtoTokenizerProxy<T>(command, type);
	}


	public static ICommand callback(final ICommand command, final ICommandCallback<Void> callback) {
		if (command instanceof ICallbackProxy<?>) {
			throw new IllegalArgumentException("callback already installed");
		}
		return new CallbackProxy<Void>(command, callback);
	}


	public static <R> ICommand callback(final IReturningCommand<R> command, final ICommandCallback<R> callback) {
		if (command instanceof ICallbackProxy<?>) {
			throw new IllegalArgumentException("callback already installed");
		}
		return new CallbackProxy<R>(command, callback);
	}


	public interface ICommandProxy
		extends ICommand {

		ICommand command();
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

		private final ICommand command;
		private final Class<T> type;


		public DtoTokenizerProxy(final ICommand command, final Class<T> type) {
			this.command = command;
			this.type = type;
		}


		public Class<T> dtoType() {
			return type;
		}


		public ICommand command() {
			return command;
		}
	}


	public interface ICallbackProxy<R>
		extends ICommandProxy {

		ICommandCallback<R> callback();
	}


	private static class CallbackProxy<R>
		implements ICallbackProxy<R> {

		private final ICommand command;
		private final ICommandCallback<R> callback;


		public CallbackProxy(final ICommand command, final ICommandCallback<R> callback) {
			this.command = command;
			this.callback = callback;
		}


		public CallbackProxy(final IReturningCommand<R> command, final ICommandCallback<R> callback) {
			this.command = command;
			this.callback = callback;
		}


		public ICommand command() {
			return command;
		}


		public ICommandCallback<R> callback() {
			return callback;
		}
	}
}
