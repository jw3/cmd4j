package cmd4j.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.SwingUtilities;

import cmd4j.Concurrency;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * Manage a set of executors for tests; 
 * 
 * @author wassj
 *
 */
public enum Services implements IService {
	t1(Mode.SINGLE),
	t2(Mode.SINGLE),
	multi1(Mode.MULTI),
	edt(Concurrency.swingExecutor()) {
		@Override
		public boolean isOwnerOfCurrentThread() {
			return SwingUtilities.isEventDispatchThread();
		}
	};

	private final Collection<Thread> threads = new ArrayList<Thread>();
	private final Mode mode;

	private ExecutorService executor;


	private Services(final ExecutorService executor) {
		this(Mode.PROVIDED);
		this.executor = executor;
	}


	private Services(final Mode mode) {
		this.mode = mode;
	}


	public boolean isOwnerOfCurrentThread() {
		return threads.contains(Thread.currentThread());
	}


	public ExecutorService executor() {
		if (executor == null) {
			executor = createExecutor(name(), mode, threads);
		}
		return executor;
	}


	public static void shutdown() {
		for (Services t : Services.values()) {
			t.executor.shutdownNow();
		}
	}


	public static ExecutorService wrap(final ThreadFactory factory) {
		return Executors.newSingleThreadExecutor(factory);
	}


	public enum Mode {
		PROVIDED,
		SINGLE,
		MULTI;
	}


	private static ExecutorService createExecutor(final String name, final Mode mode, final Collection<Thread> threads) {
		if (mode == Mode.MULTI) {
			return Executors.newCachedThreadPool(new ThreadFactory() {
				public Thread newThread(Runnable r) {
					final Thread thread = new Thread(r, name);
					threads.add(thread);
					return thread;
				}
			});
		}
		else if (mode == Mode.SINGLE) {
			return Executors.newSingleThreadExecutor(new ThreadFactory() {
				public Thread newThread(Runnable r) {
					final Thread thread = new Thread(r, name);
					threads.add(thread);
					return thread;
				}
			});
		}
		throw new IllegalStateException();
	}


	public static IService create(final String name, final Mode mode) {
		return new IService() {
			private final Collection<Thread> threads = new ArrayList<Thread>();
			private ExecutorService executor;


			public String name() {
				return name;
			}


			public boolean isOwnerOfCurrentThread() {
				return threads.contains(Thread.currentThread());
			}


			public ExecutorService executor() {
				if (executor == null) {
					executor = createExecutor(name(), mode, threads);
				}
				return executor;
			}
		};
	}


	public static IService current() {
		return new IService() {
			public String name() {
				return Thread.currentThread().getName();
			}


			public boolean isOwnerOfCurrentThread() {
				return true;
			}


			public ExecutorService executor() {
				return MoreExecutors.sameThreadExecutor();
			}
		};
	}
}
