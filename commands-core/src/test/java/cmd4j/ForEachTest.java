package cmd4j;

import java.util.Collection;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.Chains.IChainBuilder;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.IFunction;
import cmd4j.testing.CmdTests;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;

/**
 * valdidate the behavior of the {@link Commands#forEach(java.util.Collection, com.google.common.base.Supplier)} method
 * @author wassj
 *
 */
public class ForEachTest {

	@Test
	public void count()
		throws Exception {

		final TestVariable<Integer> var = TestVariable.create(0);
		final int expected = 101;
		Chains.create(Commands.forEach(CmdTests.randoms(5, expected), Suppliers.ofInstance(Does.increment(var)))).invoke();
		var.assertEquals(expected);
	}


	@Test
	public void function()
		throws Exception {

		// create a chain that (1) converts the input to uppercase (2) assert uppercase  
		final IChainBuilder builder = Chains.builder().add(toUpper()).pipe().add(assertIsUpper());

		// create and invoke a chain that runs the builder chain over each of the random strings
		Chains.create(Commands.forEach(CmdTests.randoms(5, 5), builder)).invoke();

		// same thing only this time use the input to pass the list
		Chains.create(Commands.forEach(builder)).invoke(CmdTests.randoms(5, 5));
	}


	@Test
	public void results()
		throws Exception {

		final List<String> actual = Lists.newArrayList();

		// create a chain that (1) converts the input to uppercase (2) adds the converted string to a collection 
		final IChainBuilder builder = Chains.builder().add(toUpper()).pipe().add(export(actual));

		// create and invoke a chain that runs the builder chain over each of the random strings
		final List<String> random1 = CmdTests.randoms(5, 5);
		Chains.create(Commands.forEach(random1, builder)).invoke();
		Assert.assertEquals(actual, Lists.transform(random1, Functions2.function(toUpper())));

		actual.clear();

		// same thing only this time use the input to pass the list
		final List<String> random2 = CmdTests.randoms(5, 5);
		Chains.create(Commands.forEach(builder)).invoke(random2);
		Assert.assertEquals(actual, Lists.transform(random2, Functions2.function(toUpper())));
	}


	/**
	 * function; toUpper a string
	 * @return
	 */
	private static IFunction<String, String> toUpper() {
		return new IFunction<String, String>() {
			public String invoke(final String input) {
				return Preconditions.checkNotNull(input).toUpperCase();
			}
		};
	}


	private static ICommand assertIsUpper() {
		return new ICommand2<String>() {
			public void invoke(final String input) {
				final String upper = input.toUpperCase();
				Assert.assertEquals(input, upper);
			}
		};
	}


	private static <I> ICommand export(final Collection<I> export) {
		return new ICommand2<I>() {
			public void invoke(final I input) {
				export.add(input);
			}
		};
	}
}
