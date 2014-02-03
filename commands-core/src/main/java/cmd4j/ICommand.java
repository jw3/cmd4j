package cmd4j;

import cmd4j.Chains.ILink;
import cmd4j.Observers.IObservable;

/**
 * A block of execution as detailed in the <a href="http://en.wikipedia.org/wiki/Command_pattern">Command Pattern</a>.
 * 
 * Multiple {@link ICommand commands} can be linked together using {@link ILink links} to form {@link IChain chains}.
 * Chains are then executed, resulting in the contained commands being executed in an order defined by the chain implementation.
 *
 * {@link ICommand} is the base command interface that is only used for tagging.  To be executed a command must implement one of
 * the extending types of command such as ICommand1..6
 *
 * @author wassj
 *
 */
public interface ICommand {

	/**
	 * The simplest form of a {@link ICommand command} with no return value or input input.
	 *
	 * @author wassj
	 *
	 */
	public interface ICommand1
		extends ICommand, IReturningCommand<Void> {

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
	 * A {@link ICommand command} implementation that supports having a Data Transfer Object (input) passed in at execution time.
	 * The containing {@link ILink link} is responsible for providing the input to the command at the time it is invoked.
	 * 
	 * The input may or may not be null, there is no guarantee provided in that regard.
	 * 
	 * @author wassj
	 *
	 */
	public interface ICommand2<I>
		extends ICommand, IInputCommand<I>, IReturningCommand<Void> {

		/**
		 * invoke this command
		 * @param input Data Transfer Object
		 * @throws Exception
		 */
		void invoke(I input)
			throws Exception;


		/**
		 *
		 * @author wassj
		 * @param <I>
		 */
		public interface IUndo<I>
			extends ICommand2<I>, IUndoCommand {

			void undo(I input)
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
		extends ICommand, IReturningCommand<O>, IInputCommand<I> {

		/**
		 * invoke this command, returning value of type R
		 * @param input Data Transfer Object
		 * @return the return value
		 * @throws Exception
		 */
		O invoke(I input)
			throws Exception;


		/**
		 *
		 * @author wassj
		 * @param <O>
		 * @param <I>
		 */
		public interface IUndo<I, O>
			extends ICommand4<I, O>, IUndoCommand {

			O undo(I input)
				throws Exception;
		}
	}


	/**
	 * Marks a {@link ICommand command} as being able to be executed.  Generally non-invokable interfaces to a command
	 * are used, eg. {@link ICommand}, {@link IReturningCommand}, {@link IUndoCommand}, but in case where you want to
	 * want a handle to allow command invocation, use this.
	 * 
	 * Future development will remove {@link IChain} from the public API and this will be the replacement.  So prefer
	 * using this interface in the place of IChain.
	 *
	 * @author wassj
	 * @param <O>
	 */
	public interface IInvokable<O>
		extends ICommand3<O>, ICommand4<Object, O> {
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
	 * Supports Command based 'State Machine' behavior:
	 * 
	 * {@link ICommand} implementation that supports returning a command to be run immediately upon this commands successful completion.
	 * 
	 * The returned command could be a {@link IStateCommand} and return another {@link IStateCommand}, and so on and so on until null is returned. Or
	 * the returned command could be a non-IStateCommand in which case the state path ends when that command ends.
	 * 
	 * The containing {@link ILink link} is not finished until all command paths execute.
	 * 
	 * Note that though this command returns, it is not part of the {@link IReturningCommand} hierarchy as that interface returns literal values while
	 * this interface returns functional states for immediate execution.  The distinction between literal and functional is made by not extending {@link IReturningCommand}
	 * 
	 * @author wassj
	 */
	public interface IStateCommand
		extends ICommand {

		/**
		 * a {@link IStateCommand}
		 * 
		 * @author wassj
		 */
		public interface IStateCommand1
			extends ICommand, IStateCommand {

			/**
			 * 
			 * @return Command to be executed after the successful completion of this command
			 * @throws Exception
			 */
			ICommand invoke()
				throws Exception;


			/**
			 * @author wassj
			 */
			public interface IUndo
				extends IStateCommand1, IUndoCommand {

				ICommand undo()
					throws Exception;
			}
		}


		/**
		 * {@link IStateCommand} that implements {@link IInputCommand}
		 * @author wassj
		 * @param <I>
		 */
		public interface IStateCommand2<I>
			extends ICommand, IStateCommand, IInputCommand<I> {

			/**
			 * 
			 * @param input
			 * @return Command to be executed after the successful completion of this command
			 * @throws Exception
			 */
			ICommand invoke(I input)
				throws Exception;


			/**
			 * @author wassj
			 */
			public interface IUndo<I>
				extends IStateCommand2<I>, IUndoCommand {

				ICommand undo(I input)
					throws Exception;
			}
		}


		/**
		 * a {@link IStateCommand} that returns a typed {@link IReturningCommand}
		 * 
		 * @author wassj
		 */
		public interface IStateCommand3<O>
			extends ICommand, IStateCommand, IReturningCommand<O> {

			/**
			 * 
			 * @return {@link IReturningCommand} to be executed after the successful completion of this command
			 * @throws Exception
			 */
			IReturningCommand<O> invoke()
				throws Exception;


			/**
			 * @author wassj
			 */
			public interface IUndo<O>
				extends IStateCommand3<O>, IUndoCommand {

				IReturningCommand<O> undo()
					throws Exception;
			}
		}


		/**
		 * {@link IStateCommand} that implements {@link IInputCommand} and returns a typed {@link IReturningCommand}
		 * @author wassj
		 * @param <I>
		 */
		public interface IStateCommand4<I, O>
			extends ICommand, IStateCommand, IInputCommand<I>, IReturningCommand<O> {

			/**
			 * 
			 * @param input
			 * @return Command to be executed after the successful completion of this command
			 * @throws Exception
			 */
			IReturningCommand<O> invoke(I input)
				throws Exception;


			/**
			 * @author wassj
			 */
			public interface IUndo<I, O>
				extends IStateCommand4<I, O>, IUndoCommand {

				IReturningCommand<O> undo(I input)
					throws Exception;
			}
		}
	}


	/**
	 * A {@link ICommand command} implementation that supports returning a value
	 *
	 * @author wassj
	 */
	public interface IReturningCommand<O>
		extends ICommand {
	}


	/**
	 * base interface for a pipe command
	 * @author wassj
	 */
	public interface IPipe
		extends ICommand {
	}


	/**
	 * A {@link IReturningCommand} that will set its return value as the input value for the following execution scope 
	 * @author wassj
	 * @param <O> output type
	 */
	public interface IInputPipe<O>
		extends IPipe, IReturningCommand<O> {
	}


	/**
	 * tagging interface for {@link ICommand commands} which support an input argument
	 * 
	 * @author wassj
	 * @param <I>
	 */
	public interface IInputCommand<I>
		extends ICommand {
	}


	/**
	 * a {@link IInputCommand} that receives the output value of the chain as input
	 * @author wassj
	 * @param <I> input type
	 */
	public interface IOutputPipe<I>
		extends IPipe, IInputCommand<I> {
	}


	/**
	 * convenience tagging interface for functional commands
	 * @author wassj
	 * @param <I>
	 * @param <O>
	 */
	public interface IFunction<I, O>
		extends ICommand4<I, O> {
	}


	/**
	 * a {@link IFunction}  which will redirect the previous command output into the next commands input
	 * @author wassj
	 * @param <T>
	 */
	public interface IPipeIO<T>
		extends IFunction<T, T>, IInputPipe<T>, IOutputPipe<T> {
	}


	/**
	 *
	 * @author wassj
	 */
	public interface IObservableCommand<O>
		extends IObservable<IObservableCommand<O>>, IReturningCommand<O> {
	}


	/**
	 *
	 * @author wassj
	 */
	public interface IObservableStateCommand
		extends IObservable<IObservableStateCommand>, IStateCommand {
	}
}
