package cmd4j.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;

import cmd4j.ICommand;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ILink;
import cmd4j.common.Chains.ChainBuilder;
import cmd4j.common.Commands.ICommandProxy;

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
		private ExecutorService executor;
		private boolean ignoreDtoMismatch;


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


		public ExecutorService executor() {
			return executor;
		}


		public DefaultLink executor(final ExecutorService executor) {
			this.executor = executor;
			return this;
		}


		public DefaultLink ignoreDtoMismatch(final boolean ignoreDtoMismatch) {
			this.ignoreDtoMismatch = ignoreDtoMismatch;
			return this;
		}


		/**
		 * Execute the {@link ICommand} and return the next {@link ILink}
		 */
		public ILink call()
			throws Exception {

			//		try {
			ICommand command = cmd();
			while (command != null) {
				final Object dto = dto() != null ? dto() : this.dto;
				command = invokeCommand(command, dto, ignoreDtoMismatch);
			}
			return next();
			//		}
			//		catch (Exception e) {
			//			if (failsafe) {
			//				return next();
			//			}
			//			throw e;
			//		}
		}

		/*		public ILink call()
					throws Exception {

					//		try {
					ICommand command = cmd();
					if (command instanceof IUndo) {
						((IUndo)command).undo();
					}
					return next();
					//		}
					//		catch (Exception e) {
					//			if (failsafe) {
					//				return next();
					//			}
					//			throw e;
					//		}
				}*/
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
			return new DefaultLink(command, next != null ? next.build() : null).executor(executor).dto(dto);
		}


		ILink build(boolean visits) {
			return new DefaultLink(command, next != null ? next.build(visits) : null).executor(executor).dto(dto).ignoreDtoMismatch(visits);
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


		public ILink dto(Object dto) {
			return this;
		}


		public ICommand cmd() {
			return Commands.nop();
		}


		public ExecutorService executor() {
			return null;
		}


		public ILink call() {
			return next();
		}
	}


	/*
	 * 
	 * utils
	 * 
	 * 
	 */
	/**
	 * util for executing and handling any return value from a given {@link ICommand}
	 * @param command
	 * @param dto
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	/// safely suppressed here: we do some extra checking to ensure the dto fits in the invocation
	static ICommand invokeCommand(final ICommand command, final Object dto, final boolean ignoreDtoMismatch)
		throws Exception {

		final boolean castable = dtoIsCastableForCommand(command, dto);
		if (castable) {
			if (command instanceof ICommand3) {
				return ((ICommand3)command).invoke(dto);
			}
			else if (command instanceof ICommand2) {
				((ICommand2)command).invoke(dto);
			}
			else if (command instanceof ICommand1) {
				((ICommand1)command).invoke();
			}
		}
		else if (!ignoreDtoMismatch) {
			throw new IllegalArgumentException("dto does not fit");
		}
		return null;
	}


	/**
	 * check that the passed dto fits into the passed ICommand#invoke method.
	 * @param command
	 * @param dto
	 * @return
	 */
	static boolean dtoIsCastableForCommand(final ICommand command, final Object dto) {
		if (dto != null) {
			final Class<?> cmdType = typedAs(command);
			final Class<?> dtoType = dto.getClass();
			return cmdType.isAssignableFrom(dtoType);
		}
		return true;
	}


	/**
	 * get the type parameter of the command
	 * @param t {@link ICommand} the command 
	 * @return the generic param of t, or Object if there is none
	 */
	static Class<?> typedAs(ICommand t) {
		if (!(t instanceof ICommandProxy)) {
			for (Type type : t.getClass().getGenericInterfaces()) {
				if (type instanceof ParameterizedType) {
					final Type paramType = ((ParameterizedType)type).getActualTypeArguments()[0];
					if (paramType instanceof Class<?>) {
						return (Class<?>)paramType;
					}
				}
			}
			return Object.class;
		}
		return ((ICommandProxy<?>)t).type();
	}
}
