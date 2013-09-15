package chain4j;

/**
 * The simplest form of an {@link ICommand} with no return value or input dto.
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
