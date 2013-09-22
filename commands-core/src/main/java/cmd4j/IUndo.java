package cmd4j;

/**
 * Marks an {@link ICommand} as being able to be undone.  It is entirely up to the Command implementation
 * to provide the capability to reverse the Command, this interface simply provides the calling
 * capability to run that reversing logic in the Command framework.
 * 
 * REVISIT should have DTO access
 *
 * @author wassj
 *
 */
public interface IUndo
	extends ICommand {

	/**
	 * undo this {@link ICommand}
	 * @param dto Data Transfer Object
	 * @throws Exception
	 */
	void undo(/* REVISIT add DTO */)
		throws Exception;
}
