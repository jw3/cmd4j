package cmd4j.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import cmd4j.ICommand;
import cmd4j.ICommand1;
import cmd4j.ICommand2;
import cmd4j.ICommand3;
import cmd4j.ILink;
import cmd4j.common.CmdExecutors;
import cmd4j.internal.CallableLinkDecorator.DefaultToCallable;
import cmd4j.internal.ILinker.IExecutorOf;

/**
 *
 *
 * @author wassj
 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
 *
 */
public enum Linkers {
	/*singleton-enum*/;

	public static ILinker create(final ILink head) {
		return new DefaultLinker(head);
	}


	public static Callable<Void> asCallable(final ILinker linker, final Object dto) {
		return new CallableLinkerDecorator(linker, dto);
	}


	public static ILinker makeUnthreaded(final ILinker linker) {
		return linker.executorOf(new UnthreadedExecutorOf());
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

			final ExecutorService executor = linker.executorOf().get(link, CmdExecutors.sameThreadExecutor());
			final Future<ILink> future = executor.submit(linker.toCallable().get(link, dto));
			return future.get();
		}
	}


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


	/**
	 * 
	 * @author wassj
	 *
	 */
	private static class DefaultLinker
		implements ILinker {

		private final ILink head;

		private IToCallable config = new DefaultToCallable();
		private IExecutorOf executorOf = new DefaultExecutorOf();


		public DefaultLinker(final ILink head) {
			this.head = head;
		}


		public ILink head() {
			return head;
		}


		public IToCallable toCallable() {
			return config;
		}


		public ILinker toCallable(IToCallable config) {
			this.config = config;
			return this;
		}


		public IExecutorOf executorOf() {
			return executorOf;
		}


		public ILinker executorOf(final IExecutorOf executorOf) {
			this.executorOf = executorOf;
			return this;
		}
	}


	/**
	 *
	 * @author wassj
	 *
	 */
	private static class DefaultExecutorOf
		implements IExecutorOf {

		public ExecutorService get(final ILink link, final ExecutorService defaultIfNull) {
			if (link instanceof IThreaded) {
				final IThreaded threaded = (IThreaded)link;
				return threaded.executor() != null ? threaded.executor() : defaultIfNull;
			}
			return defaultIfNull;
		}
	}


	/**
	 *
	 * @author wassj
	 *
	 */
	private static class UnthreadedExecutorOf
		implements IExecutorOf {

		public ExecutorService get(final ILink link, final ExecutorService defaultIfNull) {
			return CmdExecutors.sameThreadExecutor();
		}
	}
}
