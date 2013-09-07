package chain4j.internal;

import java.util.concurrent.ExecutorService;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.ICommand2;
import chain4j.ILink;
import chain4j.IThreaded;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * The traverser of {@link Link}s in a {@link IChain}.  It controls the execution flow from link
 * to link and ensures that each command is run by the appropriate {@link ExecutorService}.
 *
 * @author wassj
 *
 */
public class Linker
	implements FutureCallback<ILink> {

	private final Object dto;
	private final boolean unthreaded;
	private final ListeningExecutorService executor;


	public static void begin(final ILink link, final Object dto) {
		new Linker(dto, null).execute(link);
	}


	/**
	 * begin executing a link using the specified executor service
	 * @param link
	 * @param dto
	 * @param executor the default {@link ExecutorService} to use if the curent {@link ILink} is not {@link IThreaded}.  Pass null to specify unthreaded execution. 
	 */
	public static void begin(final ILink link, final Object dto, final ExecutorService executor) {
		new Linker(dto, executor).execute(link);
	}


	private Linker(final Object dto, final ExecutorService executor) {
		this.dto = dto;
		this.unthreaded = executor == null;
		this.executor = !this.unthreaded ? MoreExecutors.listeningDecorator(executor) : MoreExecutors.sameThreadExecutor();
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
	final public void onSuccess(final ILink next) {
		if (next != null) {
			this.execute(next);
		}
	}


	private void execute(final ILink link) {
		final ICommand command = link.iterator().next();
		if (command instanceof ICommand2) {
			link.dto(dto);
		}
		System.out.println("submit to " + this.executorOf(link));
		Futures.addCallback(this.executorOf(link).submit(link), this);
	}


	private ListeningExecutorService executorOf(final ILink link) {
		if (!unthreaded && link instanceof IThreaded) {
			final IThreaded threaded = (IThreaded)link;
			return threaded.executor() != null ? threaded.executor() : executor;
		}
		return executor;
	}
}
