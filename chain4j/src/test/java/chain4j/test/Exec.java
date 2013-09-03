package chain4j.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import chain4j.internal.EventDispatchExecutorService;

/**
 * Manage a set of executors for tests; 
 * 
 * @author wassj
 *
 */
public enum Exec {
	edt(EventDispatchExecutorService.create()),
	persistence(true),
	a,
	b;

	private ExecutorService executor;


	private Exec() {
	}


	private Exec(boolean daemon) {
		Executors.newSingleThreadExecutor(new ThreadFactory() {
			private Thread thread;


			public Thread newThread(Runnable r) {
				if (thread == null) {
					thread = new Thread(Exec.this.name());
					thread.setDaemon(true);
				}
				return thread;
			}
		});
	}


	private Exec(ExecutorService executor) {
		this.executor = executor;
	}


	public ExecutorService get() {
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor();
		}
		return executor;
	}


	public static void shutdown() {
		for (Exec t : Exec.values()) {
			t.executor.shutdownNow();
		}
	}
}
