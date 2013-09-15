package chain4j;

/**
 * An {@link ICommand} implementation that supports returning another {@link ICommand} instance that should
 * be run immediately upon its successful completion.
 * 
 * The returned chain could also be an {@link ICommand3} and return an {@link ICommand} value which would
 * result in it also being run.  This process will continue until a Command returns null.
 *
 * @author wassj
 *
 */
public interface ICommand3<T>
	extends ICommand {

	/**
	 * invoke this command, optionally returning an {@link ICommand} to be run upon completion of this
	 * @param dto Data Transfer Object
	 * @return {@link ICommand} to be run immediately, or null
	 * @throws Exception
	 */
	ICommand invoke(final T dto)
		throws Exception;
}
