package chain4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chain4j.internal.Link;
import chain4j.internal.Next;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Builder of {@link IChain} objects
 *
 * @author wassj
 *
 */
public class ChainBuilder {
	private LinkBuilder origin;
	private LinkBuilder current;
	private LinkBuilder finallys;
	private boolean executorsProvided;


	public static ChainBuilder create(IChainable chainable) {
		return new ChainBuilder(new LinkBuilder(chainable));
	}


	private ChainBuilder(final LinkBuilder builder) {
		this.origin = builder;
		this.current = builder;
	}


	public ChainBuilder add(IChainable chainable) {
		executorsProvided = executorsProvided && current.executor != null;
		current = current.add(chainable);
		return this;
	}


	public ChainBuilder addFinally(IChainable chainable) {
		executorsProvided = executorsProvided && current.executor != null;
		finallys = finallys != null ? finallys.add(chainable) : new LinkBuilder(chainable);
		return this;
	}


	public ChainBuilder executor(final ExecutorService executor) {
		current.executor(executor);
		return this;
	}


	public IChain build() {
		return new IChain() {
			public void exec() {
				if (finallys != null) {
					current.add(finallys);
				}
				final ExecutorService executor = !executorsProvided ? MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor()) : null;
				Next.begin(origin.build(), executor);
			}
		};
	}


	/**
	 * Sub-builder of Link objects allowing for finer grained specification of Link properties
	 * 
	 * @author wassj
	 *
	 */
	private static class LinkBuilder {
		private final IChainable chainable;
		private LinkBuilder next;
		private ListeningExecutorService executor;


		public LinkBuilder(final IChainable chainable) {
			this.chainable = chainable;
		}


		public LinkBuilder executor(final ExecutorService executor) {
			if (executor instanceof ListeningExecutorService) {
				this.executor = (ListeningExecutorService)executor;
			}
			else {
				this.executor = MoreExecutors.listeningDecorator(executor);
			}
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


		public Link build() {
			return new Link(chainable, next != null ? next.build() : null, executor);
		}
	}
}
