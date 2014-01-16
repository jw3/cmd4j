package cmd4j;

import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IFunction;

import com.google.common.base.Function;

/**
 *
 * @author wassj
 *
 */
public class Functions2 {
	/**
	 * wrap a Guava {@link Function} in a {@link IFunction}
	 * @param function
	 * @return
	 */
	public static <I, O> IFunction<I, O> function(final Function<I, O> function) {
		return new IFunction<I, O>() {
			public O invoke(final I input) {
				return function.apply(input);
			}
		};
	}


	/**
	 * wrap a {@link ICommand4} with a Guava {@link Function}
	 * @param command
	 * @return
	 */
	public static <I, O> Function<I, O> function(final ICommand4<I, O> command) {
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
	 * composites two 
	 * @param f1
	 * @param f2
	 * @return
	 */
	public static <I, OI, O> IFunction<I, O> function(final IFunction<I, ? extends OI> f1, final IFunction<OI, O> f2) {
		return new IFunction<I, O>() {
			public O invoke(final I input)
				throws Exception {

				return f2.invoke(f1.invoke(input));
			}
		};
	}


	private Functions2() {
		/*noinstance*/
	}
}
