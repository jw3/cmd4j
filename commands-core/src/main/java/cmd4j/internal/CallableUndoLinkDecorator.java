package cmd4j.internal;

import java.util.concurrent.Callable;

import cmd4j.ICommand;
import cmd4j.ILink;
import cmd4j.IUndo;
import cmd4j.internal.ILinker.IToCallable;

/**
 * Defer the callable implementation until execution time when it is installed by this decorator.
 * This will only invoke {@link IUndo#undo()} method on {@link ICommand} instances that support it.
 * {@link ICommand} invoke calls will be ignored.
 *
 * @author wassj
 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
 *
 */
public class CallableUndoLinkDecorator
	implements Callable<ILink> {

	private final ILink link;


	CallableUndoLinkDecorator(final ILink link) {
		this.link = link;
	}


	/**
	 * Execute the {@link ICommand} and return the next {@link ILink}
	 */
	public ILink call()
		throws Exception {

		//		try {
		ICommand command = link.cmd();
		if (command instanceof IUndo) {
			((IUndo)command).undo();
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


	/**
	 *
	 *
	 * @author wassj
	 *
	 */
	public static class UndoToCallable
		implements IToCallable {

		public Callable<ILink> get(final ILink link, final Object dto) {
			return new CallableUndoLinkDecorator(link);
		}
	}
}
