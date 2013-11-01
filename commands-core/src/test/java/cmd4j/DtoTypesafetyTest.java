package cmd4j;

import static cmd4j.testing.Does.invoked;
import static cmd4j.testing.Does.is;
import static cmd4j.testing.Does.var;

import org.testng.annotations.Test;

import cmd4j.testing.Does.Variable;

/**
 * Ensure that a DTO does not end up being passed to a {@link ICommand} that will not accept it.
 * This also gives a workout to the {@link Commands#tokenizeType(Class, ICommand) type tokenization}
 *
 * @author wassj
 *
 */
public class DtoTypesafetyTest {

	@Test
	public void testDtoToEmptyChain()
		throws Exception {

		Chains.create(Chains.empty()).invoke("dto");
	}


	@Test
	public void testCorrectTypes()
		throws Exception {

		{
			final Variable<Boolean> v = var(false);
			Chains.builder().add(invoked(v)).build().invoke(new String());
			v.assertEquals(true);
		}
		{
			final Variable<Boolean> v = var(false);
			Chains.builder().add(invoked(v)).build().invoke(1.1);
			v.assertEquals(true);
		}
		{
			final Variable<Boolean> v = var(false);
			Chains.builder().add(invoked(v)).build().invoke(1);
			v.assertEquals(true);
		}
	}


	@Test
	public void testCorrectTypes2()
		throws Exception {

		{
			final Variable<Boolean> v = var(false);
			Chains.builder().add(invoked(v)).build().invoke("foo");
			v.assertEquals(true);
		}
		{
			final Variable<Boolean> v = var(false);
			Chains.builder().add(invoked(Number.class, v)).build().invoke(1.1);
			v.assertEquals(true);
		}
		{
			final Variable<Boolean> v = var(false);
			Chains.builder().add(invoked(Integer.class, v)).build().invoke(1);
			v.assertEquals(true);
		}
	}


	// REVISIT can narrow this exception after some work on the exception machinery
	///@Test(expectedExceptions = IllegalArgumentException.class)
	@Test(expectedExceptions = Exception.class)
	public void testIncorrectTypes1()
		throws Exception {

		final Variable<Boolean> v = var(false);
		Chains.builder()//
			.add(invoked(Number.class, v))
			.add(is(v, false))

			.add(invoked(v))
			.add(is(v, true))

			.add(invoked(String.class, v))
			.add(is(v, true))

			.build()
			.invoke("not a number");
	}


	// REVISIT can narrow this exception after some work on the exception machinery
	///@Test(expectedExceptions = IllegalArgumentException.class)
	@Test(expectedExceptions = Exception.class)
	public void testIncorrectTypes2()
		throws Exception {

		final Variable<Boolean> v1 = var(false);
		final Variable<Boolean> v2 = var(false);
		final Variable<Boolean> v3 = var(false);

		Chains.builder()//
			.add(invoked(Integer.class, v1))
			.add(invoked(v2))
			.add(invoked(Number.class, v3))
			.build()
			.invoke(1.1);

		v1.assertEquals(false);
		v2.assertEquals(true);
		v3.assertEquals(true);
	}


	/**
	 * visitable decorator allows the incompatible commands to be skipped
	 */
	@Test
	public void testIncorrectTypes1_visitable()
		throws Exception {

		final Variable<Boolean> v1 = var(false);
		final Variable<Boolean> v2 = var(false);
		final Variable<Boolean> v3 = var(false);

		Chains.builder().add(invoked(Number.class, v1)).add(invoked(Integer.class, v2)).add(invoked(v3)).visits(true).build().invoke("not a number");

		v1.assertEquals(false);
		v2.assertEquals(false);
		v3.assertEquals(true);
	}


	/**
	 * visitable decorator allows the incompatible commands to be skipped
	 */
	@Test
	public void testIncorrectTypes2_visitable()
		throws Exception {

		final Variable<Boolean> v1 = var(false);
		final Variable<Boolean> v2 = var(false);
		final Variable<Boolean> v3 = var(false);

		Chains.builder().add(invoked(Integer.class, v1)).add(invoked(Number.class, v2)).add(invoked(v3)).visits(true).build().invoke(1.1);

		v1.assertEquals(false);
		v2.assertEquals(true);
		v3.assertEquals(true);
	}
}
