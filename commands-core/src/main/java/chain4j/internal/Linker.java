package chain4j.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.ICommand1;
import chain4j.ICommand2;
import chain4j.ICommand3;
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

		final ICommand command = head.cmd();
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
		final ListenableFuture<ILink> future = executor.submit(this.toCallable(link));
		//			if (next instanceof FutureCallback<?>) {
		//				Futures.addCallback(future, (FutureCallback<ILink>)next);
		//			}
		return future.get();
	}


	protected Callable<ILink> toCallable(final ILink link) {
		return CallableLinkDecorator.decorate(link);
	}


	private ListeningExecutorService executorOf(final ILink link) {
		if (!unthreaded && link instanceof IThreaded) {
			final IThreaded threaded = (IThreaded)link;
			return threaded.executor() != null ? threaded.executor() : executor;
		}
		return executor;
	}


	/**
	 *
	 *
	 * @author wassj
	 *
	 */
	private static class UndoLinker
		extends Linker {

		private UndoLinker(final ILink head, final Object dto, final boolean unthreaded) {
			super(head, dto, unthreaded);
		}


		@Override
		protected Callable<ILink> toCallable(ILink link) {
			return LinkUndoDecorator.decorate(link);
		}
	}


	/**
	 * Defer the callable implementation until execution time
	 * when it is installed by this decorator.
	 *
	 * @author wassj
	 *
	 */
	private static class CallableLinkDecorator
		implements Callable<ILink> {

		private final ILink link;


		public static Callable<ILink> decorate(ILink link) {
			return new CallableLinkDecorator(link);
		}


		private CallableLinkDecorator(final ILink link) {
			this.link = link;
		}


		/**
		 * Execute the {@link ICommand} and return the next link
		 */
		public ILink call()
			throws Exception {

			//		try {
			ICommand command = link.cmd();
			while (command != null) {
				command = invokeCommand(command, link.dto());
			}
			return link.next();
			//		}
			//		catch (Exception e) {
			//			if (failsafe) {
			//				return next();
			//			}
			//			throw e;
			//		}
		}

	}


	/**
	 *
	 *
	 * @author wassj
	 *
	 */
	private static class LinkUndoDecorator
		implements Callable<ILink> {

		private final ILink link;


		public static LinkUndoDecorator decorate(ILink link) {
			return new LinkUndoDecorator(link);
		}


		private LinkUndoDecorator(final ILink link) {
			this.link = link;
		}


		public ILink call()
			throws Exception {

			ICommand command = link.cmd();
			if (command instanceof ICommandUndo) {
				((ICommandUndo)command).undo();
			}
			else {
				while (command != null) {
					command = invokeCommand(command, link.dto());
				}
			}
			return link.next();
		}
	}


	/**
	 * util for executing and handling any return value from a given {@link ICommand}
	 * @param command
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	static ICommand invokeCommand(final ICommand command, final Object dto)
		throws Exception {

		if (command instanceof ICommand3) {
			return ((ICommand3)command).invoke(dto);
		}
		else if (command instanceof ICommand2) {
			((ICommand2)command).invoke(dto);
		}
		else if (command instanceof ICommand1) {
			((ICommand1)command).invoke();
		}
		return null;
	}
}
