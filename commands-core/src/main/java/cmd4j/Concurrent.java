package cmd4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.Internals.Chain.ChainCallable;
import cmd4j.Internals.Command.CommandCallable;
import cmd4j.Internals.Executor.EventDispatchExecutor;

/**
 * java.util.concurrent Command Utilities 
 *
 * @author wassj
 *
 */
public enum Concurrent {
	/*noinstance*/;

	/**
	 * submit a {@link IChain} to the {@link ExecutorService} returning the resulting {@link Future}
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static <O> Future<O> submit(final IChain<O> chain, final ExecutorService executor) {
		return executor.submit(asCallable(chain));
	}


	/**
	 * submit a {@link IChain} with input to the {@link ExecutorService} returning the resulting {@link Future}
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static <O> Future<O> submit(final IChain<O> chain, final Object input, final ExecutorService executor) {
		return executor.submit(asCallable(chain, input));
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
	 * wait on the {@link CountDownLatch} to open
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
	 * count down the {@link CountDownLatch}
	 * @param latch
	 * @return
	 */
	public static ICommand countDown(final CountDownLatch latch) {
		return new ICommand1() {
			public void invoke() {
				latch.countDown();
			}
		};
	}


	/**
	 * wrap a {@link Future} up in a {@link IReturningCommand}
	 * @param future
	 * @return
	 */
	public static <O> IReturningCommand<O> asCommand(final Future<O> future) {
		return new ICommand3<O>() {
			public O invoke()
				throws Exception {

				return future.get();
			}
		};
	}


	/**
	 * wrap a {@link Callable} up in a {@link IReturningCommand}
	 * @param future
	 * @return
	 */
	public static <O> IReturningCommand<O> asCommand(final Callable<O> callable) {
		return new ICommand3<O>() {
			public O invoke()
				throws Exception {

				return callable.call();
			}
		};
	}


	/**
	 * wrap a chain up in a {@link Callable}
	 * @param chain
	 * @return
	 */
	public static <O> Callable<O> asCallable(final IChain<O> chain) {
		return new ChainCallable<O>(chain);
	}


	/**
	 * wrap a {@link IChain} and input up in a {@link Callable}
	 * @param chain
	 * @param input
	 * @return
	 */
	public static <O> Callable<O> asCallable(final IChain<O> chain, final Object input) {
		return new ChainCallable<O>(chain, input);
	}


	/**
	 * wrap a {@link IReturningCommand} up in a {@link Callable}
	 * @param command
	 * @return
	 */
	public static <O> Callable<O> asCallable(final IReturningCommand<O> command) {
		return new CommandCallable<O>(command);
	}


	/**
	 * wrap a {@link IReturningCommand} and input up in a {@link Callable}
	 * @param command
	 * @return
	 */
	public static <O> Callable<O> asCallable(final IReturningCommand<O> command, final Object input) {
		return new CommandCallable<O>(command, input);
	}


	/**
	 * returns a new instance of an {@link ExecutorService} that submits tasks to the Swing Event Dispatch Thread
	 * @return
	 */
	public static ExecutorService swingExecutor() {
		return new EventDispatchExecutor();
	}


	/**
	 * returns a {@link ICommand} that schedules the passed {@link ExecutorService} for shutdown
	 * @param executor
	 * @return
	 */
	public static ICommand shutdown(final ExecutorService executor) {
		return new ICommand1() {
			public void invoke() {
				executor.shutdown();
			}
		};
	}
}
