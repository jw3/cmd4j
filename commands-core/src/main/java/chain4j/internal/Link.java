package chain4j.internal;

import java.util.Iterator;

import chain4j.ICommand;
import chain4j.ICommand1;
import chain4j.ICommand2;
import chain4j.ICommand3;
import chain4j.ILink;

import com.google.common.collect.Iterators;

/**
 * Provides the context in which an {@link ICommand} executes.  Can combine together with other Links to form a chain.
 *
 * @author wassj
 *
 */
public class Link
	implements ILink {

	private final ICommand command;
	private final ILink next;

	private boolean failsafe;
	private Object dto;


	public Link(final ICommand command) {
		this(command, null);
	}


	public Link(final ICommand command, final ILink next) {
		this.command = command;
		this.next = next;
	}


	public boolean isFailsafe() {
		return failsafe;
	}


	public Link setFailsafe(final boolean failsafe) {
		this.failsafe = failsafe;
		return this;
	}


	public Object dto() {
		return dto;
	}


	public Link dto(final Object dto) {
		this.dto = dto;
		return this;
	}


	public ILink next() {
		return next;
	}


	ICommand command() {
		return command;
	}


	/**
	 * Execute the {@link ICommand} and return the next link
	 */
	public ILink call()
		throws Exception {

		//		try {
		ICommand command = this.command;
		while (command != null) {
			command = Link.invokeCommand(command, dto());
		}
		return next();
		//		}
		//		catch (Exception e) {
		//			if (failsafe) {
		//				return next();
		//			}
		//			throw e;
		//		}
	}


	public Iterator<ICommand> iterator() {
		return Iterators.singletonIterator(command);
	}


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
