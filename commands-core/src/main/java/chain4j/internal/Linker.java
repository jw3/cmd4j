package chain4j.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.ICommand2;
import chain4j.ILink;
import chain4j.common.IThreaded;

import com.google.common.util.concurrent.ListenableFuture;
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
	implements Callable<Void> {

	private final Object dto;
	private final ILink head;
	private final boolean unthreaded;
	private final ListeningExecutorService executor = MoreExecutors.sameThreadExecutor();//MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());


	public static Linker create(final ILink head, final Object dto)
		throws Exception {

		return new Linker(head, dto, false);
	}


	public static Linker unthreaded(final ILink head, final Object dto)
		throws Exception {

		return new Linker(head, dto, true);
	}


	private Linker(final ILink head, final Object dto, final boolean unthreaded) {
		this.head = head;
		this.dto = dto;
		this.unthreaded = unthreaded;
	}


	public Void call()
		throws Exception {

		final ICommand command = head.iterator().next();
		if (command instanceof ICommand2) {
			head.dto(dto);
		}
		ILink next = head;
		while (next != null) {
			final ListeningExecutorService executor = this.executorOf(next);
			final ListenableFuture<ILink> future = executor.submit(next);
			//			if (next instanceof FutureCallback<?>) {
			//				Futures.addCallback(future, (FutureCallback<ILink>)next);
			//			}
			next = future.get();
		}
		return null;
	}


	private ListeningExecutorService executorOf(final ILink link) {
		if (!unthreaded && link instanceof IThreaded) {
			final IThreaded threaded = (IThreaded)link;
			return threaded.executor() != null ? threaded.executor() : executor;
		}
		return executor;
	}
}
