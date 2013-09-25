package cmd4j;

/**
 * A {@link ICommand command} implementation that supports returning another command instance that should
 * be run immediately upon this commands successful completion.
 * 
 * The returned command could also be an {@link ICommand3 command3} and return an {@link ICommand command3} 
 * which would result in it also being run.  This process will continue until a command returns null.
 * 
 * The containing {@link ILink link} is not finished until all commands execute. This provides a very simple to implement state machine behavior.
 *
 * @author wassj
 *
 */
public interface ICommand3<T>
	extends ICommand {

	/**
	 * invoke this command, optionally returning a {@link ICommand command} to be run upon completion of this
	 * @param dto Data Transfer Object
	 * @return the {@link ICommand command} to be run next, or null
	 * @throws Exception
	 */
	ICommand invoke(final T dto)
		throws Exception;
}
