package chain4j.internal;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

/**
 * An ignorantly simple implementation of an ExecutorService that runs things on the EDT
 *
 * @author wassj
 *
 */
public final class EventDispatchService
	extends AbstractExecutorService {

	private volatile boolean shutdown = false;


	public static EventDispatchService create() {
		return new EventDispatchService();
	}


	private EventDispatchService() {
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
			SwingUtilities.invokeAndWait(command);
		}
		catch (Throwable e) {
			// we shouldnt get errors here as we handle them all within the chains
			e.printStackTrace();
		}
		//SwingUtilities.invokeLater(command);
	}
}
