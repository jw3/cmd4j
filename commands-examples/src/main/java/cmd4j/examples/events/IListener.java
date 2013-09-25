package cmd4j.examples.events;

import cmd4j.IChain;
import cmd4j.ICommand;

/**
 * an example of a listener that returns {@link ICommand commands}
 *
 * @author wassj
 *
 */
public interface IListener<T> {

	/**
	 * can return a {@link ICommand command} or a {@link IChain chain}
	 */
	ICommand handle(T t);
}
