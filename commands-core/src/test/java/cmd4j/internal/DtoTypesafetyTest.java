package cmd4j.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.testng.annotations.Test;

import cmd4j.ICommand;

/**
 * Ensure that a DTO does not end up being passed to a {@link ICommand} that will not accept it.
 *
 * @author wassj
 *
 */
public class DtoTypesafetyTest {

	@Test
	public void testSameClass() {
	}


	@Test
	public void testSubclass() {
	}


	public static Class<?> typedAs(ICommand t) {
		for (Type type : t.getClass().getGenericInterfaces()) {
			if (type instanceof ParameterizedType) {
				final Type paramType = ((ParameterizedType)type).getActualTypeArguments()[0];
				if (paramType instanceof Class<?>) {
					return (Class<?>)paramType;
				}
			}
		}
		return Object.class;
	}
}
