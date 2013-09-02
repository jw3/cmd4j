package chain4j.internal;

import java.util.concurrent.ExecutorService;

import chain4j.IChain;
import chain4j.IChainable2;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * A Next instance is the traverser of {@link Link}s in a {@link IChain}.  It controls the execution flow from link
 * to link and ensures that each link is run on the appropriate executor service.
 *
 * @author wassj
 *
 */
public class Linker
	implements FutureCallback<Link> {

	private final ListeningExecutorService executor;
	private final Object dto;


	public static void begin(final Link link, final ExecutorService executor, final Object dto) {
		new Linker(dto, executor).exec(link);
	}


	private Linker(final Object dto, final ExecutorService executor) {
		this.dto = dto;
		this.executor = MoreExecutors.listeningDecorator(executor);
	}


	/**
	 * {@link FutureCallback} failure handler
	 */
	public void onFailure(final Throwable t) {
		t.printStackTrace();
	}


	/**
	 * {@link FutureCallback} success handler
	 */
	public void onSuccess(final Link next) {
		if (next != null) {
			this.exec(next);
		}
	}


	private void exec(final Link link) {
		if (link.chainable() instanceof IChainable2) {
			link.dto(dto);
		}
		System.out.println("submit to " + this.executorOf(link));
		Futures.addCallback(this.executorOf(link).submit(link), this);
	}


	private ListeningExecutorService executorOf(final Link link) {
		return link.executor() != null ? link.executor() : this.executor;
	}
}
