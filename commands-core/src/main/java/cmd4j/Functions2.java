package cmd4j;

import java.util.Collection;
import java.util.Map;

import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IFunction;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 *
 * @author wassj
 *
 */
public class Functions2 {

	/**
	 * function which returns its input
	 * @return
	 */
	public static <T> IFunction<T, T> returns() {
		return new IFunction<T, T>() {
			public T invoke(final T input) {
				return input;
			}
		};
	}


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
	 * transform the specified {@link IFunction} into a collection handling function
	 * @param function
	 * @return
	 */
	public static <I, O> IFunction<Collection<I>, Collection<O>> collection(final IFunction<I, O> function) {
		return new IFunction<Collection<I>, Collection<O>>() {
			public Collection<O> invoke(final Collection<I> inputs) {
				return Collections2.transform(inputs, function(function));
			}
		};
	}


	/**
	 * transform the specified {@link IFunction} into a collection handling function
	 * the output will be a {@link Map} containing the transformeds mapped to the original
	 * @param function
	 * @return
	 */
	public static <I, O> IFunction<Collection<I>, Map<I, O>> map(final IFunction<I, O> function) {
		return new IFunction<Collection<I>, Map<I, O>>() {
			public Map<I, O> invoke(final Collection<I> inputs) {
				return Maps.toMap(inputs, function(function));
			}
		};
	}


	/**
	 * transform the specified {@link IFunction} into a collection handling function
	 * the output will be a {@link Multimap} containing the transformeds mapped to the original
	 * @param function
	 * @return
	 */
	public static <I, O> IFunction<Collection<I>, Multimap<I, O>> multimap(final IFunction<I, Collection<O>> function) {
		return new IFunction<Collection<I>, Multimap<I, O>>() {
			public Multimap<I, O> invoke(final Collection<I> inputs) {
				final Multimap<I, O> result = HashMultimap.create();
				for (final I input : inputs) {
					result.putAll(input, function(function).apply(input));
				}
				return result;
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
