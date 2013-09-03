package chain4j;

/**
 * 
 *
 * @author wassj
 *
 */
public interface ICommand {

	void invoke()
		throws Exception;
}
