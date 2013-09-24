package cmd4j.internal;

import java.util.concurrent.ExecutorService;

import cmd4j.ILink;
import cmd4j.common.CmdExecutors;
import cmd4j.common.Links.IThreaded;
import cmd4j.internal.CmdCallables.DefaultToCallable;
import cmd4j.internal.ILinker.IExecutorOf;

/**
 *
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
		return linker.executorOf(new UnthreadedExecutorOf());
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


		public IToCallable toCallable() {
			return config;
		}


		public ILinker toCallable(IToCallable config) {
			this.config = config;
			return this;
		}


		public IExecutorOf executorOf() {
			return executorOf;
		}


		public ILinker executorOf(final IExecutorOf executorOf) {
			this.executorOf = executorOf;
			return this;
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
			return CmdExecutors.sameThreadExecutor();
		}
	}
}
