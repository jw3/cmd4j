package cmd4j;

/**
 * An {@link ICommand} implementation that supports having a Data Transfer Object (dto) passed in at execution time.
 * The containing {@link ILink} is responsible for providing the dto to the command at the time it is invoked.
 * 
 * The dto may or may not be null, there is no guarantee provided in that regard.
 * 
 * @author wassj
 *
 */
public interface ICommand2<T>
	extends ICommand {

	/**
	 * invoke this command
	 * @param dto Data Transfer Object
	 * @throws Exception
	 */
	void invoke(T dto)
		throws Exception;
}
