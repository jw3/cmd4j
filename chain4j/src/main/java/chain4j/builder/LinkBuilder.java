package chain4j.builder;

import java.util.concurrent.ExecutorService;

import chain4j.IChainable;
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
	private final IChainable chainable;
	private LinkBuilder next;
	private LinkBuilder finallys;

	private ListeningExecutorService executor;
	private Object dto;


	LinkBuilder(final IChainable chainable) {
		this.chainable = chainable;
	}


	public LinkBuilder executor(final ExecutorService executor) {
		this.executor = MoreExecutors.listeningDecorator(executor);
		return this;
	}


	public LinkBuilder add(final IChainable chainable) {
		next = new LinkBuilder(chainable);
		return next;
	}


	public LinkBuilder add(final LinkBuilder builder) {
		next = builder;
		return builder.next;
	}


	public LinkBuilder addFinally(final IChainable chainable) {
		finallys = finallys != null ? finallys.add(chainable) : new LinkBuilder(chainable);
		return this;
	}


	public LinkBuilder dto(final Object dto) {
		this.dto = dto;
		return this;
	}


	Link build() {
		return new Link(chainable, next != null ? next.build() : null, executor).dto(dto);
	}
}
