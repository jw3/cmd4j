package cmd4j;

import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author wassj
 *
 */
public class ValidateUtilsClassTests {

	@DataProvider
	public static Object[][] types() {
		return new Object[][] { {Chains.class}, {Commands.class}, {Functions2.class}, {Observers.class}};
	}


	@Test(dataProvider = "types")
	public void utils(final Class<?> type)
		throws Exception {

		final Constructor<?>[] ctors = type.getDeclaredConstructors();
		Assert.assertEquals(1, ctors.length);
		Assert.assertFalse(ctors[0].isAccessible());
		ctors[0].setAccessible(true);
		ctors[0].newInstance();
	}


	@Test
	public void internalEnums() {
		String.valueOf(Internals.Command.values());
	}
}
