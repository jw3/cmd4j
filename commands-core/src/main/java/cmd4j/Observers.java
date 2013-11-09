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
public enum Observers {
	/*noinstance*/;

	/**
	 * decorate a {@link IChain chain} with observable capability
	 * @param chain
	 * @return
	 */
	public static <O> IObservableChain<O> observable(final IChain<O> chain) {
		return Internals.Observer.observerDecorator(chain);
	}


	/**
	 * decorate a {@link ICommand command} with observable capability, supporting return values
	 * @param command
	 * @return
	 */
	public static <O> IObservableCommand<O> observable(final IReturningCommand<O> command) {
		return Internals.Observer.observerDecorator(command);
	}


	/**
	 * decorate a {@link ICommand command} with observable capability
	 * @param command
	 * @return
	 */
	public static IObservableStateCommand observable(final IStateCommand command) {
		return Internals.Observer.observerDecorator(command);
	}


	/**
	 *
	 * @author wassj
	 * @param <O>
	 */
	public interface IObservable<Ob extends IObservable<?>> {
		/**
		 * add {@link ICommand commands} that will be invoked prior to execution
		 * @return the command; decorated as observable
		 */
		Ob before(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked after execution completes
		 * invocation will occurr regardless of success/failure of the chain
		 * @return the command; decorated as observable
		 */
		Ob after(final ICommand... listeners);


		/**
		 * add {@link ICommand commands} that will be invoked upon successful completions
		 * if the command returned a result that value will be passed as the dto
		 * @return the command; decorated as observable
		 */
		Ob results(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked upon successful completions
		 * @return the command; decorated as observable
		 */
		Ob onSuccess(final ICommand... commands);


		/**
		 * add {@link ICommand commands} that will be invoked upon failed invocation of the command
		 * the cause of the failure will be available as the dto to any commands that will accept it
		 * @return the command; decorated as observable
		 */
		Ob onFailure(final ICommand... commands);
	}

}
