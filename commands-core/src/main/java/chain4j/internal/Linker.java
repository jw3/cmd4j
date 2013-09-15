package chain4j.internal;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.ICommand2;
import chain4j.ILink;
import chain4j.common.ICommandUndo;
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


	public static Linker create(final ILink head, final Object dto) {
		return new Linker(head, dto, false);
	}


	public static Linker unthreaded(final ILink head, final Object dto) {
		return new Linker(head, dto, true);
	}


	public static Linker undo(final ILink head, final Object dto) {
		return new UndoLinker(head, dto, false);
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
			next = callImpl(next);
		}
		return null;
	}


	protected ILink callImpl(final ILink link)
		throws Exception {

		final ListeningExecutorService executor = this.executorOf(link);
		final ListenableFuture<ILink> future = executor.submit(link);
		//			if (next instanceof FutureCallback<?>) {
		//				Futures.addCallback(future, (FutureCallback<ILink>)next);
		//			}
		return future.get();
	}


	private ListeningExecutorService executorOf(final ILink link) {
		if (!unthreaded && link instanceof IThreaded) {
			final IThreaded threaded = (IThreaded)link;
			return threaded.executor() != null ? threaded.executor() : executor;
		}
		return executor;
	}


	private static class UndoLinker
		extends Linker {

		private UndoLinker(final ILink head, final Object dto, final boolean unthreaded) {
			super(head, dto, unthreaded);
		}


		protected ILink callImpl(ILink link)
			throws Exception {

			return super.callImpl(LinkUndoDecorator.decorate(link));
		}
	}


	/**
	 *
	 *
	 * @author wassj
	 *
	 */
	private static class LinkUndoDecorator
		implements ILink {

		private final ILink link;


		public static ILink decorate(ILink link) {
			return new LinkUndoDecorator(link);
		}


		private LinkUndoDecorator(final ILink link) {
			this.link = link;
		}


		public ILink call()
			throws Exception {

			final ICommand command = link.iterator().next();
			if (command instanceof ICommandUndo) {
				((ICommandUndo)command).undo();
			}
			else {
				command.invoke();
			}
			return next();
		}


		public Iterator<ICommand> iterator() {
			return link.iterator();
		}


		public ILink next() {
			return link.next();
		}


		public Object dto() {
			return link.dto();
		}


		public ILink dto(Object dto) {
			link.dto(dto);
			return this;
		}
	}
}
