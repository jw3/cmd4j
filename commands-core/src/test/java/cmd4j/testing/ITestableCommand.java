package cmd4j.testing;

import cmd4j.ICommand;

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
