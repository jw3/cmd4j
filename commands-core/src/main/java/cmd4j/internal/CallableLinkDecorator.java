package cmd4j.internal;

import java.util.concurrent.Callable;

import cmd4j.ICommand;
import cmd4j.ILink;
import cmd4j.internal.ILinker.IToCallable;

/**
 * Defer the callable implementation until execution time when it is installed by this decorator.
 *
 * @author wassj
 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
 *
 */
public class CallableLinkDecorator
	implements Callable<ILink> {

	private final ILink link;
	private final Object dto;
	private final boolean ignoreDtoMismatch;


	CallableLinkDecorator(final ILink link, final Object dto, final boolean ignoreDtoMismatch) {
		this.link = link;
		this.dto = dto;
		this.ignoreDtoMismatch = ignoreDtoMismatch;
	}


	/**
	 * Execute the {@link ICommand} and return the next {@link ILink}
	 */
	public ILink call()
		throws Exception {

		//		try {
		ICommand command = link.cmd();
		while (command != null) {
			final Object dto = link.dto() != null ? link.dto() : this.dto;
			command = Linkers.invokeCommand(command, dto, ignoreDtoMismatch);
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
	public static class DefaultToCallable
		implements IToCallable {

		public Callable<ILink> get(final ILink link, final Object dto) {
			return new CallableLinkDecorator(link, dto, false);
		}
	}


	/**
	 *
	 *
	 * @author wassj
	 *
	 */
	public static class VisitableToCallable
		implements IToCallable {

		public Callable<ILink> get(final ILink link, final Object dto) {
			return new CallableLinkDecorator(link, dto, true);
		}
	}
}
