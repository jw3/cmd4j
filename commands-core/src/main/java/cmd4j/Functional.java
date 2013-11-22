package cmd4j;

import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IStateCommand.IStateCommand2;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Functional Command utils
 *
 * @author wassj
 *
 */
public class Functional {

	/**
	 * wrap a Guava {@link Function} in a {@link ICommand4}
	 * @param function
	 * @return
	 */
	public static <I, O> ICommand4<I, O> command(final Function<I, O> function) {
		return new ICommand4<I, O>() {
			public O invoke(final I input) {
				return function.apply(input);
			}
		};
	}


	/**
	 * wrap a {@link ICommand} with a Guava {@link Function}
	 * @param command
	 * @return
	 */
	public static <I, O> Function<I, O> wrap(final ICommand4<I, O> command) {
		return new Function<I, O>() {
			public O apply(final I input) {
				try {
					return command.invoke(input);
				}
				catch (final Exception e) {
					throw new RuntimeException("command invocation failed", e);
				}
			}
		};
	}


	/**
	 * invoke the {@link ICommand} if the {@link Predicate} applies to the input object
	 * @param command
	 * @param condition
	 * @return
	 */
	public static <I> IStateCommand2<I> invokeIf(final ICommand command, final Predicate<I> condition) {
		return new IStateCommand2<I>() {
			public ICommand invoke(final I input) {
				return condition.apply(input) ? command : null;
			}
		};
	}
}
