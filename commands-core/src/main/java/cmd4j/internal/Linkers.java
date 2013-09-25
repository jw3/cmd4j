package cmd4j.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ILink;
import cmd4j.common.Executors2;
import cmd4j.common.Links.IThreaded;
import cmd4j.internal.Callables.DefaultToCallable;

/**
 * A {@link ILinker linker} is a traverser of {@link ILink links} in an {@link IChain chain}.  It controls the 
 * execution flow from link to link and ensures that each command is run by the appropriate {@link ExecutorService executor}.
 * 
 * @author wassj
 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
 *
 */
public enum Linkers {
	/*singleton-enum*/;

	public static ILinker create(final ILink head) {
		return new DefaultLinker(head);
	}


	public static ILinker makeUnthreaded(final ILinker linker) {
		linker.setExecutorOf(new UnthreadedExecutorOf());
		return linker;
	}


	public interface ILinker {

		ILink head();


		IToCallable getToCallable();


		void setToCallable(IToCallable toCallable);


		IExecutorOf getExecutorOf();


		void setExecutorOf(IExecutorOf executorOf);
	}


	public interface IToCallable {
		Callable<ILink> get(ILink link, Object dto);
	}


	public interface IExecutorOf {
		ExecutorService get(ILink link, ExecutorService defaultIfNull);
	}


	/**
	 * 
	 * @author wassj
	 *
	 */
	private static class DefaultLinker
		implements ILinker {

		private final ILink head;

		private IToCallable config = new DefaultToCallable();
		private IExecutorOf executorOf = new DefaultExecutorOf();


		public DefaultLinker(final ILink head) {
			this.head = head;
		}


		public ILink head() {
			return head;
		}


		public IToCallable getToCallable() {
			return config;
		}


		public void setToCallable(IToCallable config) {
			this.config = config;
		}


		public IExecutorOf getExecutorOf() {
			return executorOf;
		}


		public void setExecutorOf(final IExecutorOf executorOf) {
			this.executorOf = executorOf;
		}
	}


	/**
	 *
	 * @author wassj
	 *
	 */
	private static class DefaultExecutorOf
		implements IExecutorOf {

		public ExecutorService get(final ILink link, final ExecutorService defaultIfNull) {
			if (link instanceof IThreaded) {
				final IThreaded threaded = (IThreaded)link;
				return threaded.executor() != null ? threaded.executor() : defaultIfNull;
			}
			return defaultIfNull;
		}
	}


	/**
	 *
	 * @author wassj
	 *
	 */
	private static class UnthreadedExecutorOf
		implements IExecutorOf {

		public ExecutorService get(final ILink link, final ExecutorService defaultIfNull) {
			return Executors2.sameThreadExecutor();
		}
	}

}
