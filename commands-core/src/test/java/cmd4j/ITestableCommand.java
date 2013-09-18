package cmd4j;

/**
 * Interface to commands that maintain state regarding their execution for testing purposes
 *
 * @author wassj
 *
 */
public interface ITestableCommand
	extends ICommand {

	/**
	 * was this command executed
	 * @return
	 */
	boolean invoked();
}
