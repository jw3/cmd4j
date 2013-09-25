package cmd4j.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import cmd4j.ICommand;
import cmd4j.ICommand.IUndo;
import cmd4j.ICommand1;
import cmd4j.ICommand2;
import cmd4j.ICommand3;
import cmd4j.ILink;
import cmd4j.common.Executors2;
import cmd4j.internal.Linkers.ILinker;
import cmd4j.internal.Linkers.IToCallable;

/**
 * Utility methods for {@link Callable}s
 *
 * @author wassj
 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
 *
 */
public enum Callables {
	/*singleton-enum*/;

	public static Callable<Void> linker(final ILinker linker, final Object dto) {
		return new CallableLinkerDecorator(linker, dto);
	}


	/**
	 *
	 *
	 * @author wassj
	 *
	 */
	private static class CallableLinkerDecorator
		implements Callable<Void> {

		private final ILinker linker;
		private final Object dto;


		public CallableLinkerDecorator(final ILinker linker, final Object dto) {
			this.linker = linker;
			this.dto = dto;
		}


		public Void call()
			throws Exception {

			ILink next = linker.head();
			while (next != null) {
				next = callImpl(next);
			}
			return null;
		}


		private ILink callImpl(final ILink link)
			throws Exception {

			final ExecutorService executor = linker.getExecutorOf().get(link, Executors2.sameThreadExecutor());
			final Future<ILink> future = executor.submit(linker.getToCallable().get(link, dto));
			return future.get();
		}
	}


	/**
	 * Defer the callable implementation until execution time when it is installed by this decorator.
	 *
	 * @author wassj
	 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
	 *
	 */
	static class CallableLinkDecorator
		implements Callable<ILink> {

		private final ILink link;
		private final Object dto;
		private final boolean ignoreDtoMismatch;


		CallableLinkDecorator(final ILink link, final Object dto, final boolean ignoreDtoMismatch) {
			this.link = link;
			this.dto = dto;
			this.ignoreDtoMismatch = ignoreDtoMismatch;
		}


		/**
		 * Execute the {@link ICommand} and return the next {@link ILink}
		 */
		public ILink call()
			throws Exception {

			//		try {
			ICommand command = link.cmd();
			while (command != null) {
				final Object dto = link.dto() != null ? link.dto() : this.dto;
				command = invokeCommand(command, dto, ignoreDtoMismatch);
			}
			return link.next();
			//		}
			//		catch (Exception e) {
			//			if (failsafe) {
			//				return next();
			//			}
			//			throw e;
			//		}
		}
	}


	/**
	 * Defer the callable implementation until execution time when it is installed by this decorator.
	 * This will only invoke {@link IUndo#undo()} method on {@link ICommand} instances that support it.
	 * {@link ICommand} invoke calls will be ignored.
	 *
	 * @author wassj
	 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
	 *
	 */
	static class CallableUndoLinkDecorator
		implements Callable<ILink> {

		private final ILink link;


		CallableUndoLinkDecorator(final ILink link) {
			this.link = link;
		}


		/**
		 * Execute the {@link ICommand} and return the next {@link ILink}
		 */
		public ILink call()
			throws Exception {

			//		try {
			ICommand command = link.cmd();
			if (command instanceof IUndo) {
				((IUndo)command).undo();
			}
			return link.next();
			//		}
			//		catch (Exception e) {
			//			if (failsafe) {
			//				return next();
			//			}
			//			throw e;
			//		}
		}
	}


	/*
	 * 
	 * 
	 * 
	 * 
	 */
	static class DefaultToCallable
		implements IToCallable {

		public Callable<ILink> get(final ILink link, final Object dto) {
			return new CallableLinkDecorator(link, dto, false);
		}
	}


	static class UndoToCallable
		implements IToCallable {

		public Callable<ILink> get(final ILink link, final Object dto) {
			return new CallableUndoLinkDecorator(link);
		}
	}


	static class VisitableToCallable
		implements IToCallable {

		public Callable<ILink> get(final ILink link, final Object dto) {
			return new CallableLinkDecorator(link, dto, true);
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
}
