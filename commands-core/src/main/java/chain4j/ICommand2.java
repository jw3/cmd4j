package chain4j;

/**
 *
 * @author wassj
 *
 */
public interface ICommand2
	extends ICommand {

	void invoke(Object dto)
		throws Exception;
}
