package cmd4j.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.ILink;
import cmd4j.internal.CmdCallables;
import cmd4j.internal.ILinker;
import cmd4j.internal.Link;
import cmd4j.internal.Linkers;

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
		if (command == null) {
			throw new IllegalArgumentException("command cannot be null");
		}

		// if created with the noarg create() method it will need initd on the first add
		if (head == null) {
			this.init(new LinkBuilder(command));
		}
		else {
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
	 * construct an {@link IChain} object from the {@link ICommand}s that have been added to this builder
	 * @return
	 */
	public IChain build() {
		return Chains.makeThreaded(this.buildImpl(), Executors.newSingleThreadExecutor());
	}


	/**
	 * @param executor
	 * @return
	 */
	public IChain build(final ExecutorService executor) {
		if (executor == null) {
			throw new IllegalArgumentException("executor cannot be null");
		}
		return Chains.makeThreaded(this.buildImpl(), executor);
	}


	public IChain buildUnthreaded() {
		return this.buildImpl();
	}


	private IChain buildImpl() {
		if (head != null) {
			if (finallys != null) {
				tail.add(finallys);
			}

			return new IChain() {
				private final ILink head = ChainBuilder.this.head.build();


				public ILink head() {
					return this.head;
				}


				public void invoke()
					throws Exception {

					this.invoke(null);
				}


				public void invoke(final Object dto)
					throws Exception {

					final ILinker linker = Linkers.create(this.head());
					CmdExecutors.sameThreadExecutor().submit(CmdCallables.linker(linker, dto)).get();
				}
			};
		}
		return Chains.empty();
	}


	/**
	 * Sub-builder of {@link Link} objects allowing for finer grained specification of Link properties
	 * 
	 * 
	 * @author wassj
	 *
	 */
	static class LinkBuilder {
		private final ICommand command;
		private LinkBuilder next;

		private ExecutorService executor;
		private Object dto;


		/**
		 * creates a new builder. package private as only the {@link ChainBuilder} should create these
		 * @param command
		 */
		LinkBuilder(final ICommand command) {
			this.command = command;
		}


		/**
		 * sets the executor for the link
		 * @param executor
		 * @return
		 */
		LinkBuilder executor(final ExecutorService executor) {
			this.executor = executor;
			return this;
		}


		LinkBuilder add(final ICommand command) {
			next = new LinkBuilder(command);
			return next;
		}


		LinkBuilder add(final LinkBuilder builder) {
			next = builder;
			return builder.next;
		}


		LinkBuilder dto(final Object dto) {
			this.dto = dto;
			return this;
		}


		ILink build() {
			final ILink link = new Link(command, next != null ? next.build() : null).dto(dto);
			if (executor != null) {
				return Links.makeThreaded(link, executor);
			}
			return link;
		}
	}
}
