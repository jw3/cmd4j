package cmd4j;

import org.testng.annotations.Test;

import cmd4j.ICommand.IFunction;
import cmd4j.testing.Asserts;
import cmd4j.testing.Does;
import cmd4j.testing.Does.Variable;

import com.google.common.base.Predicates;

/**
 *
 *
 * @author wassj
 *
 */
public class FunctionalTest {

	@Test
	public void test()
		throws Exception {

		{
			final boolean runs = true;
			final Variable<Boolean> var = Variable.create(false);
			Chains.create(Functional.invokeIf(Does.set(var, true), Predicates.alwaysTrue())).invoke();
			var.assertEquals(runs);
		}

		{
			final boolean runs = false;
			final Variable<Boolean> var = Variable.create(false);
			Chains.create(Functional.invokeIf(Does.set(var, true), Predicates.alwaysFalse())).invoke();
			var.assertEquals(runs);
		}
	}


	@Test
	public void test2()
		throws Exception {

		final int value = 101010;
		Observers.observable(Chains.builder().add(doubleIt()).ioSwap().add(stringIt()).build()).results(Asserts.is(String.valueOf(value * 2))).invoke(value);
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
}
