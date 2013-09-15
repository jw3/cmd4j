package chain4j.common;

import java.util.concurrent.ExecutorService;

import chain4j.ICommand;
import chain4j.ILink;
import chain4j.builder.LinkBuilder;
import chain4j.internal.Link;
import chain4j.internal.Linker;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Utility methods for {@link Link}s
 * 
 * @author wassj
 *
 */
public enum Links {
	/*singleton-enum*/;

	public static void execute(final ILink link)
		throws Exception {

		final Linker linker = Linker.unthreaded(link, null);
		MoreExecutors.sameThreadExecutor().submit(linker).get();
	}


	public static ILink create(final ICommand command) {
		return new LinkBuilder(command).build();
	}


	public static ILink create(final ICommand command, ExecutorService executor) {
		return new LinkBuilder(command).executor(executor).build();
	}


	public static ILink makeThreaded(final ILink link, final ExecutorService executor) {
		return new LinkThreadingDecorator(link, executor);
	}


	public static ILink handleCompletion(final ILink link, final ICompletionHandler handler) {
		return new LinkCallbackDecorator(link, handler);
	}


	/**
	 * Decorate an {@link ILink} with an {@link ExecutorService} to provide threading capability
	 *
	 * @author wassj
	 *
	 */
	private static class LinkThreadingDecorator
		implements ILink, IThreaded {

		private final ILink link;
		private final ListeningExecutorService executor;


		public LinkThreadingDecorator(final ILink link, final ExecutorService executor) {
			this.link = link;
			this.executor = MoreExecutors.listeningDecorator(executor);
		}


		public ListeningExecutorService executor() {
			return executor;
		}


		public ILink next() {
			return link.next();
		}


		public ICommand cmd() {
			return link.cmd();
		}


		public Object dto() {
			return link.dto();
		}


		public ILink dto(Object dto) {
			return link.dto(dto);
		}
	}


	/**
	 * A decorator that adds success and failure handler to an ILink
	 *
	 * @author wassj
	 *
	 */
	private static class LinkCallbackDecorator
		implements ILink, FutureCallback<ILink> {

		private final ILink link;
		private final ICompletionHandler completionHandler;


		public LinkCallbackDecorator(final ILink link, final ICompletionHandler completionHandler) {
			this.link = link;
			this.completionHandler = completionHandler;
		}


		public void onSuccess(ILink result) {
			completionHandler.onSuccess();
		}


		public void onFailure(Throwable t) {
			completionHandler.onFailure(t);
		}


		public ILink next() {
			return link.next();
		}


		public ICommand cmd() {
			return link.cmd();
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
