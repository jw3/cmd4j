package cmd4j;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.IDtoCommand;
import cmd4j.ICommand.IObservableCommand;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.Internals.Command.DtoTokenizerProxy;

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
	public static <R> R invoke(final IReturningCommand<R> command)
		throws Exception {

		return invoke(command, null);
	}


	/**
	 * execute the specified {@link ICommand command}
	 * @throws Exception
	 */
	public static <R> R invoke(final IReturningCommand<R> command, final Object dto)
		throws Exception {

		return Chains.invoke(Chains.create(command));
	}


	/**
	 * execute the specified {@link ICommand command}
	 * @throws Exception
	 */
	public static void invoke(final ICommand command)
		throws Exception {

		invoke(command, null);
	}


	/**
	 * execute the specified {@link ICommand command}
	 * @throws Exception
	 */
	public static void invoke(final ICommand command, final Object dto)
		throws Exception {

		Chains.builder().add(command).build().invoke(dto);
	}


	public static void invoke(final Collection<ICommand> commands)
		throws Exception {

		invoke(commands, null);
	}


	public static void invoke(final Collection<ICommand> commands, final Object dto)
		throws Exception {

		Chains.invoke(Chains.create(commands), dto);
	}


	public static IObservableCommand observable(final ICommand command) {
		return Internals.Command.decorator(command);
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
}
