package cmd4j;

/**
 * A block of execution in the spirit of the Command Pattern 
 * 	:REVISIT <reference>
 * 
 * Multiple Commands can be linked together using {@link ILink link} objects to form a {@link IChain chain}.
 * Chains are then executed resulting in the contained commands being executed in an order defined by the implementation.
 *
 * This is the base command interface that is only used for tagging.  An {@link ICommand} implementation alone will be skipped during normal execution.
 *
 * @author wassj
 *
 */
public interface ICommand {

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
}
