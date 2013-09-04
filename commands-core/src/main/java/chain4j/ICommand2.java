package chain4j;

/**
 * 
 * @note also considering typing this and using an abstract class which extends {@link ICommand}
 * this would allow for reflective type inspection, but would be a burden for implementors by not having an interface for dao use
 * 
 * @author wassj
 *
 */
public interface ICommand2
	extends ICommand {

	void invoke(Object dto)
		throws Exception;
}
