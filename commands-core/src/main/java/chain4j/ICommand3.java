package chain4j;

/**
 * A Command that returns its 
 *
 * @author wassj
 *
 */
public interface ICommand3<T>
	extends ICommand {

	ICommand invoke(final T dto)
		throws Exception;
}
