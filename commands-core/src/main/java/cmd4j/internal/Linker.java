package cmd4j.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.ICommand1;
import cmd4j.ICommand2;
import cmd4j.ICommand3;
import cmd4j.ILink;
import cmd4j.common.IUndo;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * The traverser of {@link Link}s in a {@link IChain}.  It controls the execution flow from link
 * to link and ensures that each command is run by the appropriate {@link ExecutorService}.
 * 
 * @dto A Linker contains the default dto that is used if an individual Link does not specify its own dto.
 * @concurrency A Linker always executes on the thread it was called from.
 *
 * @author wassj
 *
 */
public class Linker
	implements Callable<Void> {

	private final Object dto;
	private final ILink head;
	private final boolean unthreaded;
	private final ListeningExecutorService executor = MoreExecutors.sameThreadExecutor();

	private boolean visitable;


	/**
	 * Marks an object for execution by a specified {@link ListeningExecutorService}
	 *
	 * @author wassj
	 *
	 */
	public interface IThreaded {

		ListeningExecutorService executor();
	}


	public static Linker create(final ILink head, final Object dto) {
		return new Linker(head, dto, false);
	}


	public static Linker unthreaded(final ILink head, final Object dto) {
		return new Linker(head, dto, true);
	}


	public static Linker undo(final ILink head, final Object dto) {
		return new UndoLinker(head, dto, false);
	}


	private Linker(final ILink head, final Object dto, final boolean unthreaded) {
		this.head = head;
		this.dto = dto;
		this.unthreaded = unthreaded;
	}


	/**
	 * set this linker to visitable mode
	 * this allows dto commands that do not match the chain type to be skipped by the chain
	 * with visitable mode turned off a mismatched dto will result in an exception being thrown
	 * @param visitable
	 * @return
	 */
	public Linker visitable(final boolean visitable) {
		this.visitable = visitable;
		return this;
	}


	public Void call()
		throws Exception {

		ILink next = head;
		while (next != null) {
			next = callImpl(next);
		}
		return null;
	}


	protected ILink callImpl(final ILink link)
		throws Exception {

		final ListeningExecutorService executor = this.executorOf(link);
		final ListenableFuture<ILink> future = executor.submit(this.toCallable(link));
		//			if (next instanceof FutureCallback<?>) {
		//				Futures.addCallback(future, (FutureCallback<ILink>)next);
		//			}
		return future.get();
	}


	protected Callable<ILink> toCallable(final ILink link) {
		return CallableLinkDecorator.decorate(link, dto);
	}


	private ListeningExecutorService executorOf(final ILink link) {
		if (!unthreaded && link instanceof IThreaded) {
			final IThreaded threaded = (IThreaded)link;
			return threaded.executor() != null ? threaded.executor() : executor;
		}
		return executor;
	}


	protected Object linkerDto() {
		return dto;
	}


	/**
	 *
	 *
	 * @author wassj
	 *
	 */
	private static class UndoLinker
		extends Linker {

		private UndoLinker(final ILink head, final Object dto, final boolean unthreaded) {
			super(head, dto, unthreaded);
		}


		@Override
		protected Callable<ILink> toCallable(ILink link) {
			return LinkUndoDecorator.decorate(link, this.linkerDto());
		}
	}


	/**
	 * Defer the callable implementation until execution time
	 * when it is installed by this decorator.
	 *
	 * @author wassj
	 *
	 */
	private static class CallableLinkDecorator
		implements Callable<ILink> {

		private final ILink link;
		private final Object dto;


		public static Callable<ILink> decorate(final ILink link, final Object dto) {
			return new CallableLinkDecorator(link, dto);
		}


		private CallableLinkDecorator(final ILink link, final Object dto) {
			this.link = link;
			this.dto = dto;
		}


		/**
		 * Execute the {@link ICommand} and return the next link
		 */
		public ILink call()
			throws Exception {

			//		try {
			ICommand command = link.cmd();
			while (command != null) {
				final Object dto = link.dto() != null ? link.dto() : this.dto;
				command = invokeCommand(command, dto);
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
	 *
	 *
	 * @author wassj
	 *
	 */
	private static class LinkUndoDecorator
		implements Callable<ILink> {

		private final ILink link;
		private final Object dto;


		public static LinkUndoDecorator decorate(final ILink link, final Object dto) {
			return new LinkUndoDecorator(link, dto);
		}


		private LinkUndoDecorator(final ILink link, final Object dto) {
			this.link = link;
			this.dto = dto;
		}


		public ILink call()
			throws Exception {

			ICommand command = link.cmd();
			if (command instanceof IUndo) {
				((IUndo)command).undo();
			}
			else {
				while (command != null) {
					final Object dto = link.dto() != null ? link.dto() : this.dto;
					command = invokeCommand(command, dto);
				}
			}
			return link.next();
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
	/// safely suppress here: we do some extra checking to ensure the dto fits in the invocation
	static ICommand invokeCommand(final ICommand command, final Object dto)
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
		else {
			throw new IllegalArgumentException("dto does not fit");
		}
		return null;
	}


	/**
	 * check that the passed dto fits into the passed commands invoke(T) method.
	 *  
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
	 * @param t
	 * @return
	 */
	private static Class<?> typedAs(ICommand t) {
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
