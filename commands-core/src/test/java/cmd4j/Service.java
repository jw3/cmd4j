package cmd4j;

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
public enum Service {
	t1,
	t2,
	edt(EventDispatchExecutor.create()) {
		@Override
		public boolean isCurrent() {
			return SwingUtilities.isEventDispatchThread();
		}
	};

	private ExecutorService executor;
	private Thread thread;


	private Service() {
		Executors.newSingleThreadExecutor(new ThreadFactory() {
			public Thread newThread(Runnable r) {
				if (thread == null) {
					thread = new Thread(Service.this.name());
				}
				return thread;
			}
		});
	}


	private Service(ExecutorService executor) {
		this.executor = executor;
	}


	public boolean isCurrent() {
		return Thread.currentThread() == thread;
	}


	public ExecutorService executor() {
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor();
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

}
