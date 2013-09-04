package chain4j.internal;

import java.util.concurrent.ExecutorService;

import chain4j.IChain;
import chain4j.ICommand2;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * The traverser of {@link Link}s in a {@link IChain}.  It controls the execution flow from link
 * to link and ensures that each command is run by the appropriate executor.
 *
 * @author wassj
 *
 */
public class Linker
	implements FutureCallback<Link> {

	private final Object dto;
	private final boolean unthreaded;
	private final ListeningExecutorService executor;


	public static void begin(final Link link, final Object dto, final boolean unthreaded, final ExecutorService executor) {
		new Linker(dto, unthreaded, executor).execute(link);
	}


	private Linker(final Object dto, final boolean unthreaded, final ExecutorService executor) {
		this.dto = dto;
		this.unthreaded = unthreaded;
		this.executor = MoreExecutors.listeningDecorator(executor);
	}


	/**
	 * {@link FutureCallback} failure handler
	 */
	final public void onFailure(final Throwable t) {
		t.printStackTrace();
	}


	/**
	 * {@link FutureCallback} success handler
	 */
	final public void onSuccess(final Link next) {
		if (next != null) {
			this.execute(next);
		}
	}


	private void execute(final Link link) {
		if (link.command() instanceof ICommand2) {
			link.dto(dto);
		}
		System.out.println("submit to " + this.executorOf(link));
		Futures.addCallback(this.executorOf(link).submit(link), this);
	}


	private ListeningExecutorService executorOf(final Link link) {
		return !unthreaded && link.executor() != null ? link.executor() : this.executor;
	}
}
