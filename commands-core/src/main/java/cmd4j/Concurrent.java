package cmd4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.Internals.Chain.ChainCallable;
import cmd4j.Internals.Command.CommandCallable;
import cmd4j.Internals.Executor.EventDispatchExecutor;
import cmd4j.Internals.Executor.SameThreadExecutorService;

/**
 * java.util.concurrent Command Utilities 
 *
 * @author wassj
 *
 */
public enum Concurrent {
	/*noinstance*/;

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
	 * 
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static <O> Future<O> submit(final IChain<O> chain, final ExecutorService executor) {
		return executor.submit(asCallable(chain));
	}


	/**
	 * 
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static <I, O> Future<O> submit(final IChain<O> chain, final I dto, final ExecutorService executor) {
		return executor.submit(asCallable(chain, dto));
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
	 * 
	 * @param chain
	 * @return
	 */
	public static <O> Callable<O> asCallable(final IChain<O> chain) {
		return new ChainCallable<Void, O>(chain);
	}


	/**
	 * 
	 * @param chain
	 * @param dto
	 * @return
	 */
	public static <I, O> Callable<O> asCallable(final IChain<O> chain, final I dto) {
		return new ChainCallable<I, O>(chain, dto);
	}


	/**
	 * 
	 * @param chain
	 * @return
	 */
	public static <O> Callable<O> asCallable(final IReturningCommand<O> command) {
		return new CommandCallable<O>(command);
	}


	/**
	 * 
	 * @param chain
	 * @param dto
	 * @return
	 */
	public static <O> Callable<O> asCallable(final IReturningCommand<O> command, final Object dto) {
		return new CommandCallable<O>(command, dto);
	}


	public static ExecutorService swingExecutor() {
		return new EventDispatchExecutor();
	}


	public static ExecutorService sameThreadExecutor() {
		return new SameThreadExecutorService();
	}


	public static ICommand shutdown(final ExecutorService executor) {
		return new ICommand1() {
			public void invoke() {
				executor.shutdown();
			}
		};
	}
}
