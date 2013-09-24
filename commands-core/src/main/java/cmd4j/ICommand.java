package cmd4j;

/**
 * A block of execution in the spirit of the GOF Command Pattern.
 *  command: 
 * 
 * Multiple Commands can be linked together using {@link ILink} objects to form a {@link IChain}.
 * Chains are then executed resulting in the commands being executed in the sequence they were built.
 *
 * This is a base class that serves as a tagging interface only.
 *
 * @author wassj
 *
 */
public interface ICommand {

	/**
	 * Marks an {@link ICommand} as being able to be undone.  It is entirely up to the Command implementation
	 * to provide the capability to reverse the Command, this interface simply provides the calling
	 * capability to run that reversing logic in the Command framework.
	 * 
	 * @author wassj
	 *
	 */
	public interface IUndo
		extends ICommand {

		/**
		 * undo this {@link ICommand}
		 * @throws Exception
		 */
		void undo()
			throws Exception;
	}
}
