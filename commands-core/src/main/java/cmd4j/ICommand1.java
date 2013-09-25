package cmd4j;

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
