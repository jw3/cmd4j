package cmd4j.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ILink;
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


	public interface ILinker {

		ILink head();


		IToCallable getToCallable();


		void setToCallable(IToCallable toCallable);
	}


	public interface IToCallable {
		Callable<ILink> get(ILink link, Object dto);
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
	}
}
