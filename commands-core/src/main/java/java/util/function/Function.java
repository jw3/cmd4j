package java.util.function;

/**
 * This is a functional interface and can therefore be used as the assignment target for a lambda expression or method reference.
 * 
 * (placeholder for Java8 Function)
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public interface Function<T, R> {

	/**
	 * Applies this function to the given argument.
	 * @param t the function argument
	 * @return the function result
	 */
	R apply(T t);
}
