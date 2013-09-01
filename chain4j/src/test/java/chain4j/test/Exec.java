package chain4j.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chain4j.internal.EventDispatchService;

/**
 * Manage a set of executors for tests; 
 * 
 * @author wassj
 *
 */
public enum Exec {
	edt(EventDispatchService.create()),
	a,
	b,
	c;

	private ExecutorService executor;


	private Exec() {
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
