package cmd4j.internal;

import cmd4j.ICommand;
import cmd4j.ILink;

/**
 * Provides the context in which an {@link ICommand} executes.  Can combine together with other Links to form a chain.
 * 
 * @dto A Link can provide an overriding dto that will be passed to commands executing withing the context of this Link.
 * @concurrency A Link does not exhibit any concurrency behavior by default, but can be decorated to do so. 
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


	public ICommand cmd() {
		return command;
	}


	public ILink next() {
		return next;
	}
}
