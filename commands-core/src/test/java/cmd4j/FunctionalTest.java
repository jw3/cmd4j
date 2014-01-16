package cmd4j;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IFunction;
import cmd4j.ICommand.IInputPipe;
import cmd4j.ICommand.IOutputPipe;
import cmd4j.ICommand.IPipeIO;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.testing.Asserts;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;

import com.google.common.base.Function;
import com.google.common.base.Predicates;

/**
 *
 *
 * @author wassj
 *
 */
public class FunctionalTest {

	@Test
	public void compose()
		throws Exception {

		final int value = 10101;
		final int result = Functions2.function(doubleIt(), doubleIt()).invoke(value);
		Assert.assertEquals(result, value * 4);
	}


	/*
	 * pipes dont work on the dto, the dto is already the input
	 */
	@Test
	public void pipe0()
		throws Exception {

		final String actual = Chains.create(Commands.<String> pipe()).invoke("someValue");
		Assert.assertNull(actual);
	}


	@Test
	public void pipe1()
		throws Exception {

		{
			final TestVariable<String> actual = TestVariable.create();
			final String expected = UUID.randomUUID().toString().substring(0, 7);
			Chains.create(Does.returns(expected), Commands.<String> pipe(), Does.set(actual)).invoke();
			actual.assertEquals(expected);
		}
		{
			final TestVariable<String> actual = TestVariable.create();
			final String expected = UUID.randomUUID().toString().substring(0, 7);
			Chains.create(Does.returns(expected), Commands.<String> pipe(), Does.set(actual)).invoke("notexpected");
			actual.assertEquals(expected);
		}
	}


	@Test
	public void pipe2()
		throws Exception {

		final int value = 101010;
		Observers.observable(Chains.builder().add(doubleIt()).add(Commands.pipe()).add(stringIt()).build()).results(Asserts.is(String.valueOf(value * 2))).invoke(value);
	}


	@Test
	public void pipe3()
		throws Exception {

		Chains.builder().add(Asserts.is(100)).add(intToString()).add(Commands.pipe()).add(Asserts.is("100")).build().invoke(100);
	}


	@Test
	public void fn1()
		throws Exception {

		{
			final boolean runs = true;
			final TestVariable<Boolean> var = TestVariable.create(false);
			Chains.create(Commands.invokeIf(Does.set(var, true), Predicates.alwaysTrue())).invoke();
			var.assertEquals(runs);
		}

		{
			final boolean runs = false;
			final TestVariable<Boolean> var = TestVariable.create(false);
			Chains.create(Commands.invokeIf(Does.set(var, true), Predicates.alwaysFalse())).invoke();
			var.assertEquals(runs);
		}
	}


	/*
	 * validate that the else command is executed when the predicate fails, and ignored when it passes
	 */
	@Test
	public void fn2()
		throws Exception {

		{
			final boolean runs = true;
			final TestVariable<Boolean> var = TestVariable.create(false);
			final TestVariable<Boolean> var2 = TestVariable.create(false);
			Chains.create(Commands.invokeIf(Does.set(var, true), Predicates.alwaysTrue(), Does.set(var2, true))).invoke();
			var.assertEquals(runs);
			var2.assertEquals(!runs);
		}

		{
			final boolean runs = false;
			final TestVariable<Boolean> var = TestVariable.create(false);
			final TestVariable<Boolean> var2 = TestVariable.create(false);
			Chains.create(Commands.invokeIf(Does.set(var, true), Predicates.alwaysFalse(), Does.set(var2, true))).invoke();
			var.assertEquals(runs);
			var2.assertEquals(!runs);
		}
	}


	@Test
	public void transform1()
		throws Exception {

		final String lower = "this is some lower case text";
		final TestVariable<String> actual = TestVariable.create();
		Chains.create(Does.returns(lower), transformStringToUppercase(), Does.set(actual)).invoke();
		actual.assertEquals(lower.toUpperCase());
	}


	@Test
	public void transform2()
		throws Exception {

		final String value = String.valueOf(Integer.MAX_VALUE);
		final TestVariable<Integer> actual = TestVariable.create();
		Chains.create(Does.returns(value), transformStringToInteger(), Does.set(actual)).invoke();
		actual.assertEquals(Integer.MAX_VALUE);
	}


	@Test
	public void toGuavaTransform1() {
		final String lower = "this is some lower case text";
		final String actual = Functions2.function(transformStringToUppercase()).apply(lower);
		Assert.assertEquals(actual, lower.toUpperCase());
	}


	@Test
	public void fromGuavaTransform1()
		throws Exception {

		final String lower = "this is some lower case text";
		final String actual = Chains.create(Functions2.function(new Function<String, String>() {
			public String apply(String input) {
				return input.toUpperCase();
			}
		})).invoke(lower);
		Assert.assertEquals(actual, lower.toUpperCase());
	}


	@Test(expectedExceptions = RuntimeException.class)
	public void fromGuavaTransform_withException()
		throws Exception {

		Functions2.function(new ICommand4<Object, Object>() {
			public Object invoke(Object input)
				throws Exception {
				throw new Exception();
			}
		}).apply("");
	}


	/*
	 * fails due to the string value not being an integer
	 */
	@Test(expectedExceptions = NumberFormatException.class)
	public void transform3()
		throws Exception {

		final String value = "not an integer";
		final TestVariable<Integer> actual = TestVariable.create();
		Chains.create(Does.returns(value), transformStringToInteger(), Does.set(actual)).invoke();
		actual.assertEquals(Integer.MAX_VALUE);
	}


	public ICommand4<Integer, String> intToString() {
		return new ICommand4<Integer, String>() {
			public String invoke(final Integer input) {
				return String.valueOf(input);
			}
		};
	}


	private static IFunction<Integer, Integer> doubleIt() {
		return new IFunction<Integer, Integer>() {
			public Integer invoke(final Integer input) {
				return input * 2;
			}
		};
	}


	private static IFunction<Object, String> stringIt() {
		return new IFunction<Object, String>() {
			public String invoke(final Object input) {
				return String.valueOf(input);
			}
		};
	}


	private static ICommand4<String, String> transformStringToUppercase() {
		return new IPipeIO<String>() {
			public String invoke(final String input) {
				return input != null ? input.toUpperCase() : null;
			}
		};
	}


	private static IReturningCommand<Integer> transformStringToInteger() {
		class transform
			implements IFunction<String, Integer>, IInputPipe<Integer>, IOutputPipe<String> {

			public Integer invoke(final String input) {
				return Integer.valueOf(input);
			}
		}
		return new transform();
	}
}
