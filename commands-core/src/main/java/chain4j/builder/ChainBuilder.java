package chain4j.builder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.ILink;
import chain4j.decorator.ChainThreadingDecorator;
import chain4j.internal.AbstractChain;
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
		return new ChainThreadingDecorator(this.buildImpl(), Executors.newSingleThreadExecutor());
	}


	/**
	 * pass null to specify unthreaded execution
	 * @param executor
	 * @return
	 */
	public IChain build(final ExecutorService executor) {
		return new ChainThreadingDecorator(this.buildImpl(), executor);
	}


	public IChain buildUnthreaded() {
		return this.buildImpl();
	}


	private IChain buildImpl() {
		if (head != null) {
			if (finallys != null) {
				tail.add(finallys);
			}
			return new AbstractChain(head.build()) {
				public void exec() {
					Linker.begin(this.head(), this.dto());
				}
			};
		}
		return new EmptyChain();
	}


	/**
	 * An empty {@link IChain} implementation
	 * 
	 * @author wassj
	 *
	 */
	private static class EmptyChain
		implements IChain {

		private Object dto;


		public void exec() {
		}


		public ILink head() {
			return null;
		}


		public boolean isEmpty() {
			return true;
		}


		public Object dto() {
			return dto;
		}


		public IChain dto(Object dto) {
			this.dto = dto;
			return this;
		}
	}
}
