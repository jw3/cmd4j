package cmd4j.internal;

import cmd4j.ICommand;
import cmd4j.ILink;

/**
 * Provides the context in which an {@link ICommand} executes.  Can combine together with other Links to form a chain.
 * 
 * @author wassj
 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
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


	public ICommand cmd() {
		return command;
	}


	public ILink next() {
		return next;
	}
}
