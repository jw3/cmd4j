package cmd4j;

import cmd4j.Chains.ILink;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;
import cmd4j.Observers.IObservable;

/**
 * A container of a set of {@link ILink links} that will execute sequentially. 
 * 
 * Provides an empty context in which those links execute, and can be decorated to add 
 * to that context things such as concurrency and undo support.
 * 
 * Implements {@link ICommand3} and {@link ICommand4} in order to:
 *  1) Allow chains to be linked with other chains just as a {@link ICommand command} would.
 *  2) Provide dual means of invocation, {@link #invoke()} and {@link #invoke(Object)}.  The later
 *     of which will specify the Data Transfer Object for the Chain.
 *  3) Provide return value capability
 *
 * @author wassj
 * 
 * @see ICommand
 * @see ILink
 *
 */
public interface IChain<O>
	extends ICommand3<O>, ICommand4<Object, O> {

	/**
	 * the first {@link ILink} that will be called when this chain executes.
	 * @return {@link ILink} the first link
	 */
	ILink head();


	/**
	 * 
	 * @author wassj
	 * @param <O>
	 */
	public interface IUndoChain<O>
		extends IChain<O>, ICommand3.IUndo<O>, ICommand4.IUndo<Object, O> {
	}


	/**
	 * an {@link IChain} that can have observer commands added to it
	 * @author wassj
	 */
	public interface IObservableChain<O>
		extends IChain<O>, IObservable<IObservableChain<O>> {
	}
}
