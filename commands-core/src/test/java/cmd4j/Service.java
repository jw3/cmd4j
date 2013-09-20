package cmd4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.SwingUtilities;

import cmd4j.internal.EventDispatchExecutor;

/**
 * Manage a set of executors for tests; 
 * 
 * @author wassj
 *
 */
public enum Service implements IService {
	t1(Mode.SINGLE),
	t2(Mode.SINGLE),
	multi10(Mode.MULTI),
	edt(EventDispatchExecutor.create()) {
		@Override
		public boolean isCurrent() {
			return SwingUtilities.isEventDispatchThread();
		}
	};

	private final Collection<Thread> threads = new ArrayList<Thread>();
	private final Mode mode;

	private ExecutorService executor;


	private Service(final ExecutorService executor) {
		this(Mode.PROVIDED);
		this.executor = executor;
	}


	private Service(final Mode mode) {
		this.mode = mode;
	}


	public boolean isCurrent() {
		return threads.contains(Thread.currentThread());
	}


	public ExecutorService executor() {
		if (executor == null) {
			executor = createExecutor(name(), mode, threads);
		}
		return executor;
	}


	public static void shutdown() {
		for (Service t : Service.values()) {
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


			public boolean isCurrent() {
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
}
