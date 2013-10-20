package cmd4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cmd4j.ICommand.ICommand1;

/**
 * Factory and utility methods for {@link ExecutorService executors}
 *
 * @author wassj
 *
 */
public enum Executors2 {
	/*singleton-enum*/;

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


	/******************************************************************************
	 * 
	 * 
	 * 
	 * begin private implementation details
	 * 
	 * 
	 * 
	 ******************************************************************************/

	/**
	 * A basic implementation of an ExecutorService that runs things on the EDT
	 * 
	 * @author wassj
	 */
	private static class EventDispatchExecutor
		extends AbstractExecutorService {

		private final Logger logger = LoggerFactory.getLogger(EventDispatchExecutor.class);
		private volatile boolean shutdown = false;


		private EventDispatchExecutor() {
		}


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


		public boolean awaitTermination(long timeout, TimeUnit unit) {
			return false;
		}


		public void execute(Runnable command) {
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
	private static class SameThreadExecutorService
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
