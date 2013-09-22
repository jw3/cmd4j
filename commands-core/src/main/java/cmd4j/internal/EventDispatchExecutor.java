package cmd4j.internal;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;

/**
 * A basic implementation of an ExecutorService that runs things on the EDT
 * 
 * REVISIT should synchronize this make it a singleton
 *
 * @author wassj
 *
 */
@Beta
public final class EventDispatchExecutor
	extends AbstractExecutorService {

	private final Logger logger = LoggerFactory.getLogger(EventDispatchExecutor.class);
	private volatile boolean shutdown = false;


	public static EventDispatchExecutor create() {
		return new EventDispatchExecutor();
	}


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
