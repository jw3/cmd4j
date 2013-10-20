package cmd4j;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;

/**
 * A container of a set of {@link ILink links} that will execute sequentially. 
 * 
 * Provides an empty context in which those links execute, and can be decorated to add 
 * to that context things such as concurrency and undo support. 
 * 
 * Implements {@link ICommand1} and {@link ICommand2} in order to:
 *  1) Allow chains to be linked with other chains just as a {@link ICommand command} would.
 *  2) Provide dual means of invocation, {@link #invoke()} and {@link #invoke(Object)}.  The later
 *     of which will specify the Data Transfer Object for the Chain.
 *
 * @author wassj
 * 
 * @see ICommand
 * @see ILink
 *
 */
public interface IChain
	extends ICommand1, ICommand2<Object> {

	/**
	 * the first {@link ILink} that will be called when this chain executes.
	 * @return {@link ILink} the first link
	 */
	ILink head();


	/**
	 *
	 * @author wassj
	 */
	public interface IObservableChain
		extends IChain {

		/**
		 * add {@link ICommand commands} that will be invoked prior to the {@link IChain chain} execution
		 * @return the chain, decorated
		 */
		IObservableChain before(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked after the {@link IChain chain} execution completes
		 * invocation will occurr regardless of success/failure of the chain
		 * @return the chain, decorated
		 */
		IObservableChain after(final ICommand... listeners);


		/**
		 * add {@link ICommand commands} that will be invoked upon successful invocation of the {@link IChain chain}
		 * @return the chain, decorated
		 */
		IObservableChain onSuccess(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked upon failed invocation of the {@link IChain chain}
		 * the cause of the failure will be available as the dto to any commands that will accept it
		 * @return the chain, decorated
		 */
		IObservableChain onFailure(final ICommand... commands);
	}
}
