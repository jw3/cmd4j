package chain4j.builder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import chain4j.IChain;
import chain4j.ICommand;
import chain4j.decorator.ChainThreadingDecorator;
import chain4j.internal.AbstractChain;
import chain4j.internal.Linker;

/**
 * Builder pattern implementation for creating {@link IChain} objects
 * 
 * There are two types of {@link IChain}s that can be created, static and dynamic.  This builder will assemble a static 
 *
 * @author wassj
 *
 */
final public class ChainBuilder {
	private LinkBuilder head;
	private LinkBuilder tail;
	private LinkBuilder finallys;


	/**
	 * creates an empty {@link IChain} which can be used in any operation a normal chain would, but will not do anything
	 * @return {@link IChain} the empty chain
	 */
	public static IChain empty() {
		return new EmptyChain();
	}


	/**
	 * creates a new {@link ChainBuilder}
	 * @return {@link ChainBuilder} a new builder
	 */
	public static ChainBuilder create() {
		return new ChainBuilder();
	}


	/**
	 * creates a new {@link ChainBuilder} with the passed {@link ICommand} used as head
	 * @param command becomes the head command in the chain
	 * @return new {@link ChainBuilder}
	 * @throws IllegalArgumentException if command is null
	 */
	public static ChainBuilder create(final ICommand command) {
		if (command == null) {
			throw new IllegalArgumentException("command cannot be null");
		}
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
				public void run() {
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
		extends AbstractChain {

		public EmptyChain() {
			super(LinkBuilder.empty());
		}


		public void run() {
			System.out.println("executed empty chain");
		}
	}
}
