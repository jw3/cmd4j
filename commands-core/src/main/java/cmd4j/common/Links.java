package cmd4j.common;

import java.util.concurrent.ExecutorService;

import cmd4j.ICommand;
import cmd4j.IDoneListener;
import cmd4j.ILink;
import cmd4j.common.ChainBuilder.LinkBuilder;
import cmd4j.internal.Link;

import com.google.common.util.concurrent.FutureCallback;

/**
 * Utility methods for {@link Link}s
 * 
 * @author wassj
 *
 */
public enum Links {
	/*singleton-enum*/;

	/**
	 * creates an empty {@link ILink} that can be used anywhere a normal link is used but will not do anything 
	 * @return
	 */
	public static ILink empty() {
		return new EmptyLink();
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


	public static ILink handleCompletion(final ILink link, final IDoneListener listener) {
		return new LinkCallbackDecorator(link, listener);
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
		private final ExecutorService executor;


		public LinkThreadingDecorator(final ILink link, final ExecutorService executor) {
			this.link = link;
			this.executor = executor;
		}


		public ExecutorService executor() {
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
		private final IDoneListener listener;


		public LinkCallbackDecorator(final ILink link, final IDoneListener listener) {
			this.link = link;
			this.listener = listener;
		}


		public void onSuccess(ILink result) {
			listener.onSuccess();
		}


		public void onFailure(Throwable t) {
			listener.onException(t);
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
	}


	private static class EmptyLink
		implements ILink {

		public ILink next() {
			return null;
		}


		public Object dto() {
			return null;
		}


		public ICommand cmd() {
			return Commands.nop();
		}
	}


	/**
	 * Marks an object for execution by a specified {@link ExecutorService}
	 *
	 * @author wassj
	 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
	 *
	 */
	public interface IThreaded {
		ExecutorService executor();
	}

}
