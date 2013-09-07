package chain4j.builder;

import java.util.concurrent.ExecutorService;

import chain4j.ICommand;
import chain4j.ILink;
import chain4j.decorator.LinkThreadingDecorator;
import chain4j.internal.Link;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Sub-builder of Link objects allowing for finer grained specification of Link properties
 * 
 * @author wassj
 *
 */
final public class LinkBuilder {
	private final ICommand command;
	private LinkBuilder next;
	private LinkBuilder finallys;

	private ListeningExecutorService executor;
	private Object dto;


	LinkBuilder(final ICommand command) {
		this.command = command;
	}


	public LinkBuilder executor(final ExecutorService executor) {
		this.executor = MoreExecutors.listeningDecorator(executor);
		return this;
	}


	public LinkBuilder add(final ICommand command) {
		next = new LinkBuilder(command);
		return next;
	}


	public LinkBuilder add(final LinkBuilder builder) {
		next = builder;
		return builder.next;
	}


	public LinkBuilder addFinally(final ICommand command) {
		finallys = finallys != null ? finallys.add(command) : new LinkBuilder(command);
		return this;
	}


	public LinkBuilder dto(final Object dto) {
		this.dto = dto;
		return this;
	}


	ILink build() {
		final ILink link = new Link(command, next != null ? next.build() : null).dto(dto);
		if (executor != null) {
			return new LinkThreadingDecorator(link, executor);
		}
		return link;
	}
}
