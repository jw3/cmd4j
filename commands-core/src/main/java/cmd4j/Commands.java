package cmd4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;

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
		return new Command2Proxy<T>(command, type);
	}


	/**
	 * tokenize the type parameter of the command
	 * useful only when erasure is removing necessary type information
	 * @param type
	 * @param command
	 * @return
	 */
	public static <T> ICommand tokenize(Class<T> type, ICommand3<T> command) {
		return new Command3Proxy<T>(command, type);
	}


	/**
	 * Provide the means of storing the acceptable dto type on a command, useful as a workaround to erasure.
	 *
	 * @author wassj
	 */
	public interface ICommandProxy<T>
		extends ICommand {

		Class<T> type();


		ICommand command();
	}


	private static class Command2Proxy<T>
		implements ICommandProxy<T>, ICommand2<T> {

		private final Class<T> type;
		private final ICommand2<T> command;


		public Command2Proxy(final ICommand2<T> command, final Class<T> type) {
			this.command = command;
			this.type = type;
		}


		public Class<T> type() {
			return type;
		}


		public ICommand command() {
			return command;
		}


		public void invoke(final T dto)
			throws Exception {

			command.invoke(dto);
		}
	}


	private static class Command3Proxy<T>
		implements ICommandProxy<T>, ICommand3<T> {

		private final Class<T> type;
		private final ICommand3<T> command;


		public Command3Proxy(final ICommand3<T> command, final Class<T> type) {
			this.command = command;
			this.type = type;
		}


		public Class<T> type() {
			return type;
		}


		public ICommand command() {
			return command;
		}


		public ICommand invoke(final T dto)
			throws Exception {

			return command.invoke(dto);
		}
	}
}
