package cmd4j;

import static cmd4j.testing.Does.is;
import static cmd4j.testing.Does.var;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2;
import cmd4j.testing.Does.Variable;

/**
 * Ensure that a Input does not end up being passed to a {@link ICommand} that will not accept it.
 * This also gives a workout to the {@link Commands#tokenizeType(Class, ICommand) type tokenization}
 *
 * @author wassj
 *
 */
public class InputTypesafetyTest {

	static ICommand invoked(final Variable<Boolean> called) {
		return new ICommand2<Object>() {
			public void invoke(final Object input) {
				called.setValue(true);
			}
		};
	}


	static ICommand invokedString(final Variable<Boolean> called) {
		return new ICommand2<String>() {
			public void invoke(final String input) {
				called.setValue(true);
			}
		};
	}


	static ICommand invokedNumber(final Variable<Boolean> called) {
		return new ICommand2<Number>() {
			public void invoke(final Number input) {
				called.setValue(true);
			}
		};
	}


	static ICommand invokedInteger(final Variable<Boolean> called) {
		return new ICommand2<Integer>() {
			public void invoke(final Integer input) {
				called.setValue(true);
			}
		};
	}


	static ICommand invokedDouble(final Variable<Boolean> called) {
		return new ICommand2<Double>() {
			public void invoke(final Double input) {
				called.setValue(true);
			}
		};
	}


	@Test
	public void testInputToEmptyChain()
		throws Exception {

		Chains.create(Chains.create()).invoke("input");
	}


	@Test
	public void testCorrectTypes()
		throws Exception {

		{
			final Variable<Boolean> v = var(false);
			Chains.builder().add(invokedString(v)).build().invoke(new String());
			v.assertEquals(true);
		}
		{
			final Variable<Boolean> v = var(false);
			Chains.builder().add(invokedDouble(v)).build().invoke(1.1);
			v.assertEquals(true);
		}
		{
			final Variable<Boolean> v = var(false);
			Chains.builder().add(invokedInteger(v)).build().invoke(1);
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
			.add(invokedInteger(v))
			.add(is(v, false))

			.add(invokedString(v))
			.add(is(v, true))

			.add(invokedString(v))
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
		final Variable<Boolean> v4 = var(false);

		Chains.builder()//
			.add(invokedInteger(v1))
			.add(invokedString(v2))
			.add(invokedNumber(v3))
			.add(invoked(v4))
			.build()
			.invoke(1.1);

		v1.assertEquals(false);
		v2.assertEquals(false);
		v3.assertEquals(true);
		v4.assertEquals(true);
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
		final Variable<Boolean> v4 = var(false);

		Chains.builder().add(invokedNumber(v1)).add(invokedInteger(v2)).add(invokedString(v3)).add(invoked(v4)).visits(true).build().invoke("not a number");

		v1.assertEquals(false);
		v2.assertEquals(false);
		v3.assertEquals(true);
		v4.assertEquals(true);
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
		final Variable<Boolean> v4 = var(false);

		Chains.builder().add(invokedInteger(v1)).add(invokedNumber(v2)).add(invokedString(v3)).add(invoked(v4)).visits(true).build().invoke(1.1);

		v1.assertEquals(false);
		v2.assertEquals(true);
		v3.assertEquals(false);
		v4.assertEquals(true);
	}
}
