package chain4j.internal;

import java.util.Iterator;

import chain4j.ICommand;
import chain4j.ICommand2;
import chain4j.ILink;

import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Provides the context in which an {@link ICommand} executes.  Can combine together with other Links to form a chain.
 *
 * @author wassj
 *
 */
public class Link
	implements ILink {

	private final ListeningExecutorService executor;
	private final ICommand command;
	private final ILink next;

	private boolean failsafe;
	private Object dto;


	public Link(final ICommand command) {
		this(command, null, null);
	}


	public Link(final ICommand command, final Link next) {
		this(command, next, null);
	}


	public Link(final ICommand command, final ListeningExecutorService executor) {
		this(command, null, executor);
	}


	public Link(final ICommand command, ILink next, final ListeningExecutorService executor) {
		this.command = command;
		this.next = next;
		this.executor = executor;
	}


	public ListeningExecutorService executor() {
		return executor;
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
