package chain4j.internal;

import java.util.Iterator;

import chain4j.ICommand;
import chain4j.ICommand2;
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
		if (command instanceof ICommand2) {
			this.dto = dto;
		}
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

		try {
			if (dto != null) {
				((ICommand2)command).invoke(dto);
			}
			else {
				command.invoke();
			}
			return next();
		}
		catch (Exception e) {
			if (failsafe) {
				return next();
			}
			throw e;
		}
	}


	public Iterator<ICommand> iterator() {
		return Iterators.singletonIterator(command);
	}
}
