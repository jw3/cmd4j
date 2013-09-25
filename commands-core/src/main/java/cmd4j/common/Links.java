package cmd4j.common;

import java.util.concurrent.ExecutorService;

import cmd4j.ICommand;
import cmd4j.ILink;
import cmd4j.common.Chains.ChainBuilder;

/**
 * Utility methods for {@link ILink links}
 * 
 * @author wassj
 *
 */
public enum Links {
	/*singleton-enum*/;

	/**
	 * creates an empty {@link ILink link} that can be used anywhere a normal link is used but will not do anything 
	 */
	public static ILink empty() {
		return new EmptyLink();
	}


	public static ILink create(final ICommand command) {
		return new LinkBuilder(command).build();
	}


	public static ILink create(final ICommand command, ExecutorService executor) {
		return new LinkBuilder(command).executor(executor).build();
	}


	public static ILink makeThreaded(final ILink link, final ExecutorService executor) {
		return new LinkThreadingDecorator(link, executor);
	}


	/**
	 * Marks a {@link ILink link} for execution by a specified {@link ExecutorService executor}
	 *
	 * @author wassj
	 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
	 */
	public interface IThreaded {
		ExecutorService executor();
	}


	/******************************************************************************
	 * 
	 * 
	 * 
	 * begin private implementation details
	 * 
	 * 
	 * 
	 ******************************************************************************/

	/**
	 * Provides the context in which a {@link ICommand command} executes.  
	 * Can combine together with other {@link ILink links} to form a chain.
	 * 
	 * @author wassj
	 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
	 */
	static class DefaultLink
		implements ILink {

		private final ICommand command;
		private final ILink next;

		private Object dto;


		public DefaultLink(final ICommand command) {
			this(command, null);
		}


		public DefaultLink(final ICommand command, final ILink next) {
			this.command = command;
			this.next = next;
		}


		public Object dto() {
			return dto;
		}


		public DefaultLink dto(final Object dto) {
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


	/**
	 * Builder pattern implementation for {@link ILink links}
	 * 
	 * @author wassj
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
			final ILink link = new DefaultLink(command, next != null ? next.build() : null).dto(dto);
			if (executor != null) {
				return Links.makeThreaded(link, executor);
			}
			return link;
		}
	}


	/**
	 * Decorate a {@link ILink link} with an {@link ExecutorService executor} to provide threading capability
	 *
	 * @author wassj
	 */
	private static class LinkThreadingDecorator
		implements ILink, IThreaded {

		private final ILink link;
		private final ExecutorService executor;


		public LinkThreadingDecorator(final ILink link, final ExecutorService executor) {
			this.link = link;
			this.executor = executor;
		}


		public ExecutorService executor() {
			return executor;
		}


		public ILink next() {
			return link.next();
		}


		public ICommand cmd() {
			return link.cmd();
		}


		public Object dto() {
			return link.dto();
		}
	}


	/**
	 * An empty {@link ILink link}
	 *
	 * @author wassj
	 *
	 */
	private static class EmptyLink
		implements ILink {

		public ILink next() {
			return null;
		}


		public Object dto() {
			return null;
		}


		public ICommand cmd() {
			return Commands.nop();
		}
	}
}
