package cmd4j;

import cmd4j.IChain.IObservableChain;
import cmd4j.ICommand.IObservableCommand;
import cmd4j.ICommand.IObservableStateCommand;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.ICommand.IStateCommand;

/**
 * chain/command observer utils 
 *
 * @author wassj
 *
 */
public class Observers {

	/**
	 * decorate a {@link IChain chain} with observable capability
	 * @param chain
	 * @return
	 */
	public static <O> IObservableChain<O> observable(final IChain<O> chain) {
		return Internals.Observer.decorator(chain);
	}


	/**
	 * decorate a {@link ICommand command} with observable capability, supporting return values
	 * @param command
	 * @return
	 */
	public static <O> IObservableCommand<O> observable(final IReturningCommand<O> command) {
		return Internals.Observer.decorator(command);
	}


	/**
	 * decorate a {@link ICommand command} with observable capability
	 * @param command
	 * @return
	 */
	public static IObservableStateCommand observable(final IStateCommand command) {
		return Internals.Observer.decorator(command);
	}


	/**
	 * Defines a wrapper that allows for listening at different points in a commands lifecycle.
	 * 
	 * All commands set in an observer method will be treated as visitors
	 * 
	 * @author wassj
	 * @param <O>
	 */
	public interface IObservable<T extends IObservable<?>> {
		/**
		 * add {@link ICommand commands} that will be invoked prior to execution
		 * @return the command; decorated as observable
		 */
		T before(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked after execution completes
		 * invocation will occurr regardless of success/failure of the chain
		 * @return the command; decorated as observable
		 */
		T after(final ICommand... listeners);


		/**
		 * add {@link ICommand commands} that will be invoked upon successful completions
		 * if the command returned a result that value will be passed as the input
		 * @return the command; decorated as observable
		 */
		T results(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked upon successful completions
		 * @return the command; decorated as observable
		 */
		T onSuccess(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked upon failed invocation of the command
		 * the cause of the failure will be available as the input to any commands that will accept it
		 * @return the command; decorated as observable
		 */
		T onFailure(final ICommand... commands);
	}


	private Observers() {
		/*noinstance*/
	}
}
