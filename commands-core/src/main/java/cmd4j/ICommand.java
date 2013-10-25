package cmd4j;

/**
 * A block of execution as detailed in the <a href="http://en.wikipedia.org/wiki/Command_pattern">Command Pattern</a>.
 * 
 * Multiple {@link ICommand commands} can be linked together using {@link ILink links} to form {@link IChain chains}.
 * Chains are then executed resulting in the contained commands being executed in an order defined by the implementation.
 *
 * This is the base command interface that is only used for tagging.  An {@link ICommand} implementation alone will be skipped during normal execution.
 *
 * @author wassj
 *
 */
public interface ICommand {

	/**
	 * The simplest form of a {@link ICommand command} with no return value or input dto.
	 *
	 * @author wassj
	 *
	 */
	public interface ICommand1
		extends ICommand {

		/**
		 * invoke this command
		 * @throws Exception
		 */
		void invoke()
			throws Exception;


		/**
		 *
		 * @author wassj
		 */
		public interface IUndo
			extends ICommand1, IUndoCommand {

			void undo()
				throws Exception;
		}
	}


	/**
	 * A {@link ICommand command} implementation that supports having a Data Transfer Object (dto) passed in at execution time.
	 * The containing {@link ILink link} is responsible for providing the dto to the command at the time it is invoked.
	 * 
	 * The dto may or may not be null, there is no guarantee provided in that regard.
	 * 
	 * @author wassj
	 *
	 */
	public interface ICommand2<T>
		extends ICommand, IDtoCommand<T> {

		/**
		 * invoke this command
		 * @param dto Data Transfer Object
		 * @throws Exception
		 */
		void invoke(T dto)
			throws Exception;


		/**
		 *
		 * @author wassj
		 * @param <T>
		 */
		public interface IUndo<T>
			extends ICommand2<T>, IUndoCommand {

			void undo(T dto)
				throws Exception;
		}
	}


	/**
	 *
	 * @see IReturningCommand 
	 * @author wassj
	 * @param <R>
	 */
	public interface ICommand3<R>
		extends ICommand, IReturningCommand<R> {

		R invoke()
			throws Exception;


		/**
		 *
		 * @author wassj
		 * @param <R>
		 */
		public interface IUndo<R>
			extends ICommand3<R>, IUndoCommand {

			R undo()
				throws Exception;
		}
	}


	/**
	 *
	 * @see IReturningCommand
	 * @author wassj
	 * @param <T>
	 * @param <R>
	 */
	public interface ICommand4<R, T>
		extends ICommand, IReturningCommand<R>, IDtoCommand<T> {

		/**
		 * invoke this command, returning value of type R
		 * @param dto Data Transfer Object
		 * @return the return value
		 * @throws Exception
		 */
		R invoke(T dto)
			throws Exception;


		/**
		 *
		 * @author wassj
		 * @param <R>
		 * @param <T>
		 */
		public interface IUndo<R, T>
			extends ICommand4<R, T> {

			R undo(T dto)
				throws Exception;
		}
	}


	/**
	 * Marks a {@link ICommand command} as being able to be undone.  It is entirely up to the Command implementation
	 * to provide the capability to reverse the Command, this interface simply provides the calling
	 * capability to run that reversing logic in the Command framework.
	 * 
	 * This is a tagging interface only, see the subtypes of this:
	 * 
	 * @author wassj
	 *
	 */
	public interface IUndoCommand
		extends ICommand {
	}


	/**
	 * A {@link ICommand command} implementation that supports returning a value.  If that value happens to be another command instance, it will
	 * be run immediately upon this commands successful completion.
	 * 
	 * The returned command could also be a {@link IReturningCommand} and return another {@link IReturningCommand}, so on and so on until a non {@link ICommand}
	 * value (or null) is returned.
	 * 
	 * The containing {@link ILink link} is not finished until all commands execute. This provides a very simple to implement state machine behavior.
	 *
	 * @author wassj
	 *
	 */
	public interface IReturningCommand<R>
		extends ICommand {
	}


	/**
	 *
	 * @author wassj
	 * @param <T>
	 */
	public interface IDtoCommand<T>
		extends ICommand {
	}


	/**
	 *
	 * @author wassj
	 * @param <O>
	 */
	public interface IObservable<O extends IObservable> {
		/**
		 * add {@link ICommand commands} that will be invoked prior to execution
		 * @return the command; decorated as observable
		 */
		O before(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked after execution completes
		 * invocation will occurr regardless of success/failure of the chain
		 * @return the command; decorated as observable
		 */
		O after(final ICommand... listeners);


		/**
		 * add {@link ICommand commands} that will be invoked upon successful completions
		 * if the command returned a result that value will be passed as the dto
		 * @return the command; decorated as observable
		 */
		O results(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked upon successful completions
		 * @return the command; decorated as observable
		 */
		O onSuccess(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked upon failed invocation of the command
		 * the cause of the failure will be available as the dto to any commands that will accept it
		 * @return the command; decorated as observable
		 */
		O onFailure(final ICommand... commands);
	}


	/**
	 *
	 * @author wassj
	 */
	public interface IObservableCommand
		extends IObservable<IObservableCommand>, ICommand {
	}
}
