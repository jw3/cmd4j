package cmd4j;

import cmd4j.Observers.IObservable;

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
	public interface ICommand2<I>
		extends ICommand, IDtoCommand<I> {

		/**
		 * invoke this command
		 * @param dto Data Transfer Object
		 * @throws Exception
		 */
		void invoke(I dto)
			throws Exception;


		/**
		 *
		 * @author wassj
		 * @param <I>
		 */
		public interface IUndo<I>
			extends ICommand2<I>, IUndoCommand {

			void undo(I dto)
				throws Exception;
		}
	}


	/**
	 *
	 * @see IReturningCommand 
	 * @author wassj
	 * @param <O>
	 */
	public interface ICommand3<O>
		extends ICommand, IReturningCommand<O> {

		O invoke()
			throws Exception;


		/**
		 *
		 * @author wassj
		 * @param <O>
		 */
		public interface IUndo<O>
			extends ICommand3<O>, IUndoCommand {

			O undo()
				throws Exception;
		}
	}


	/**
	 *
	 * @see IReturningCommand
	 * @author wassj
	 * @param <I>
	 * @param <O>
	 */
	public interface ICommand4<I, O>
		extends ICommand, IReturningCommand<O>, IDtoCommand<I> {

		/**
		 * invoke this command, returning value of type R
		 * @param dto Data Transfer Object
		 * @return the return value
		 * @throws Exception
		 */
		O invoke(I dto)
			throws Exception;


		/**
		 *
		 * @author wassj
		 * @param <O>
		 * @param <I>
		 */
		public interface IUndo<I, O>
			extends ICommand4<I, O>, IUndoCommand {

			O undo(I dto)
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
	public interface IReturningCommand<O>
		extends ICommand {
	}


	/**
	 *
	 * @author wassj
	 * @param <I>
	 */
	public interface IDtoCommand<I>
		extends ICommand {
	}


	/**
	 *
	 * @author wassj
	 */
	public interface IObservableCommand<O>
		extends IObservable<IObservableCommand<O>>, IReturningCommand<O> {
	}
}
