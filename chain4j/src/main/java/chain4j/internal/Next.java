package chain4j.internal;

import java.util.concurrent.ExecutorService;

import chain4j.IChain;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * A Next instance is the traverser of {@link Link}s in a {@link IChain}.  It controls the execution flow from link
 * to link and ensures that each link is run on the appropriate executor service.
 *
 * @author wassj
 *
 */
public class Next
	implements FutureCallback<Link> {

	private final ListeningExecutorService executor;


	public static void begin(Link link, ExecutorService executor) {
		final Next next = new Next(executor);

		final ListenableFuture<Link> f = link.getExecutor().submit(link);
		Futures.addCallback(f, next);
	}


	public Next(ExecutorService executor) {
		if (executor instanceof ListeningExecutorService) {
			this.executor = (ListeningExecutorService)executor;
		}
		else {
			this.executor = MoreExecutors.listeningDecorator(executor);
		}
	}


	public void onFailure(Throwable t) {
		t.printStackTrace();
	}


	public void onSuccess(Link next) {
		if (next != null) {
			final ListenableFuture<Link> f = next.getExecutor().submit(next);
			Futures.addCallback(f, this);
		}
		else if (executor != null) {
			executor.shutdownNow();
		}
	}
}
