package chain4j.builder;

import java.util.concurrent.ExecutorService;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.internal.AbstractChain;
import chain4j.internal.Link;
import chain4j.internal.Linker;

/**
 * Builder of {@link IChain} objects
 *
 * @author wassj
 *
 */
final public class ChainBuilder {
	private LinkBuilder head;
	private LinkBuilder tail;
	private LinkBuilder finallys;


	public static IChain empty() {
		return new EmptyChain();
	}


	/**
	 * create an empty builder
	 * @return
	 */
	public static ChainBuilder create() {
		return new ChainBuilder();
	}


	/**
	 * create and initialize a builder with the passed command as the starting point
	 * @param command
	 * @return
	 */
	public static ChainBuilder create(final ICommand command) {
		return new ChainBuilder().init(new LinkBuilder(command));
	}


	/**
	 * init the builder with the passed builder as head (and tail for now)
	 * @param builder
	 * @return
	 */
	private ChainBuilder init(final LinkBuilder builder) {
		this.head = builder;
		this.tail = builder;
		return this;
	}


	/**
	 * add the command to the end of the chain
	 * @param command
	 * @return
	 */
	public ChainBuilder add(final ICommand command) {
		if (command != null) {
			tail = tail != null ? tail.add(command) : new LinkBuilder(command);
		}
		return this;
	}


	/**
	 * add the command as a 'finally' command
	 * @param command
	 * @return
	 */
	public ChainBuilder addFinally(final ICommand command) {
		if (command != null) {
			finallys = finallys != null ? finallys.add(command) : new LinkBuilder(command);
		}
		return this;
	}


	/**
	 * set the executor for the tail link
	 * @param executor
	 * @return
	 */
	public ChainBuilder executor(final ExecutorService executor) {
		if (tail == null) {
			throw new NullPointerException("chain builder was not initialized, tail is null");
		}
		tail.executor(executor);
		return this;
	}


	/**
	 * set an individual overriding dto for the tail link
	 * @param dto
	 * @return
	 */
	public ChainBuilder dto(final Object dto) {
		if (tail == null) {
			throw new NullPointerException("chain builder was not initialized, tail is null");
		}
		tail.dto(dto);
		return this;
	}


	/**
	 * get the {@link LinkBuilder} for tail
	 * @return
	 */
	public LinkBuilder linkBuilder() {
		return tail;
	}


	/**
	 * construct an {@link IChain} object from the {@link ICommand}s that have been added to this builder
	 * @return
	 */
	public IChain build() {
		if (head != null) {
			if (finallys != null) {
				tail.add(finallys);
			}
			return new AbstractChain(head.build()) {
				public void exec() {
					Linker.begin(this.head(), this.executor(), this.dto());
				}
			};
		}
		return new EmptyChain();
	}


	/**
	 * An empty {@link IChain} implementation that will be used as a placeholder
	 * 
	 * @author wassj
	 *
	 */
	private static class EmptyChain
		implements IChain {

		public void exec() {
		}


		public boolean isEmpty() {
			return true;
		}


		public Link head() {
			return null;
		}


		public IChain dto(Object dto) {
			return this;
		}


		public IChain executor(ExecutorService executor) {
			return this;
		}
	}
}
