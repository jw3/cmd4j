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
	}


	public interface ICommand1_1<R>
		extends ICommand, IReturningCommand<R> {

		R invoke()
			throws Exception;
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
		extends ICommand {

		/**
		 * invoke this command
		 * @param dto Data Transfer Object
		 * @throws Exception
		 */
		void invoke(T dto)
			throws Exception;
	}


	public interface ICommand2_1<T, R>
		extends ICommand, IReturningCommand<R> {

		R invoke(T dto)
			throws Exception;
	}


	/**
	 * A {@link ICommand command} implementation that supports returning another command instance that should
	 * be run immediately upon this commands successful completion.
	 * 
	 * The returned command could also be an {@link ICommand3 command3} and return an {@link ICommand command3} 
	 * which would result in it also being run.  This process will continue until a command returns null.
	 * 
	 * The containing {@link ILink link} is not finished until all commands execute. This provides a very simple to implement state machine behavior.
	 *
	 * @author wassj
	 *
	 */
	public interface ICommand3<T>
		extends ICommand2_1<T, ICommand>, IReturningCommand<ICommand> {

		/**
		 * invoke this command, optionally returning a {@link ICommand command} to be run upon completion of this
		 * @param dto Data Transfer Object
		 * @return the {@link ICommand command} to be run next, or null
		 * @throws Exception
		 */
		ICommand invoke(final T dto)
			throws Exception;
	}


	public interface ICommand3_0
		extends ICommand1_1<ICommand>, IReturningCommand<ICommand> {

		ICommand invoke()
			throws Exception;
	}


	/**
	 * Marks a {@link ICommand command} as being able to be undone.  It is entirely up to the Command implementation
	 * to provide the capability to reverse the Command, this interface simply provides the calling
	 * capability to run that reversing logic in the Command framework.
	 * 
	 * @author wassj
	 *
	 */
	public interface IUndo
		extends ICommand {

		/**
		 * undo this command
		 * @throws Exception
		 */
		void undo()
			throws Exception;
	}


	public interface IReturningCommand<T>
		extends ICommand {
	}


	public interface ICommandCallback<T> {
		void onSuccess(T returns);


		void onFailure(Exception e);
	}
}
