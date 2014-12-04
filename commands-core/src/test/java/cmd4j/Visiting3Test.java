package cmd4j;

import java.util.UUID;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.IInputCommand;

/**
 * Validate the behavior of a {@link IChain} visiting mode
 * 
 * Demonstrate a chain that contains {@link IInputCommand commands} that accept input of varying types.
 * Pass this chain various input types and test the behavior when in and out of visiting mode.
 * 
 * @author wassj
 *
 */
public class Visiting3Test {
	@Mocked
	private ICommand2<Object> object;
	@Mocked
	private ICommand2<String> string;
	@Mocked
	private ICommand2<Number> number;
	@Mocked
	private ICommand2<Integer> integer;
	@Mocked
	private ICommand2<IFoo> foo;
	@Mocked
	private ICommand2<IBar> bar;
	@Mocked
	private ICommand2<IFooBar> foobar;


	@Test
	public void visitingString()
		throws Exception {

		final String input = UUID.randomUUID().toString();
		new Expectations() {
			{
				object.invoke(input);
				times = 1;
				string.invoke(input);
				times = 1;
				number.invoke(anyInt);
				times = 0;
				integer.invoke(anyInt);
				times = 0;
			}
		};
		Chains.builder().add(object).add(string).add(number).add(integer).visits(true).build().invoke(input);
	}


	@Test(expectedExceptions = IllegalArgumentException.class)
	public void visitingStringFail()
		throws Exception {

		final String input = UUID.randomUUID().toString();
		Chains.builder().add(object).add(string).add(number).add(integer).visits(false).build().invoke(input);
	}


	@Test
	public void visitingObject()
		throws Exception {

		final Object input = new Object();
		new Expectations() {
			{
				object.invoke(input);
				times = 1;
				string.invoke(anyString);
				times = 0;
				number.invoke(anyInt);
				times = 0;
				integer.invoke(anyInt);
				times = 0;
			}
		};
		Chains.builder().add(object).add(string).add(number).add(integer).visits(true).build().invoke(input);
	}


	@Test(expectedExceptions = IllegalArgumentException.class)
	public void visitingObjectFail()
		throws Exception {

		final Object input = new Object();
		Chains.builder().add(object).add(string).add(number).add(integer).visits(false).build().invoke(input);
	}


	@Test
	public void visitingNumber()
		throws Exception {

		final Double input = 1.1D;
		new Expectations() {
			{
				object.invoke(input);
				times = 1;
				string.invoke(anyString);
				times = 0;
				number.invoke(anyDouble);
				times = 1;
				integer.invoke(anyInt);
				times = 0;
			}
		};
		Chains.builder().add(object).add(string).add(number).add(integer).visits(true).build().invoke(input);
	}


	@Test(expectedExceptions = IllegalArgumentException.class)
	public void visitingNumberFail()
		throws Exception {

		final Object input = new Object();
		Chains.builder().add(object).add(string).add(number).add(integer).visits(false).build().invoke(input);
	}


	@Test
	public void visitingInteger()
		throws Exception {

		final Integer input = 1;
		new Expectations() {
			{
				object.invoke(input);
				times = 1;
				string.invoke(anyString);
				times = 0;
				number.invoke(anyInt);
				times = 1;
				integer.invoke(anyInt);
				times = 1;
			}
		};
		Chains.builder().add(object).add(string).add(number).add(integer).visits(true).build().invoke(input);
	}


	@Test(expectedExceptions = IllegalArgumentException.class)
	public void visitingIntegerFail()
		throws Exception {

		final Integer input = 1;
		Chains.builder().add(object).add(string).add(number).add(integer).visits(false).build().invoke(input);
	}


	@Test
	public void visitingFoo(@Mocked final IFoo input)
		throws Exception {

		new Expectations() {
			{
				object.invoke(any);
				times = 1;
				string.invoke(anyString);
				times = 0;
				number.invoke(anyDouble);
				times = 0;
				integer.invoke(anyInt);
				times = 0;
				foo.invoke(input);
				times = 1;
				bar.invoke(null);
				times = 0;
				foobar.invoke(null);
				times = 0;
			}
		};
		Chains.builder().add(object).add(string).add(number).add(integer).add(foo).add(bar).add(foobar).visits(true).build().invoke(input);
	}


	@Test(expectedExceptions = IllegalArgumentException.class)
	public void visitingFooFail(@Mocked final IFoo input)
		throws Exception {

		Chains.builder().add(object).add(string).add(number).add(integer).add(foo).add(bar).add(foobar).visits(false).build().invoke(input);
	}


	@Test
	public void visitingBar(@Mocked final IBar input)
		throws Exception {

		new Expectations() {
			{
				object.invoke(any);
				times = 1;
				string.invoke(anyString);
				times = 0;
				number.invoke(anyDouble);
				times = 0;
				integer.invoke(anyInt);
				times = 0;
				foo.invoke(null);
				times = 0;
				bar.invoke(input);
				times = 1;
				foobar.invoke(null);
				times = 0;
			}
		};
		Chains.builder().add(object).add(string).add(number).add(integer).add(foo).add(bar).add(foobar).visits(true).build().invoke(input);
	}


	@Test(expectedExceptions = IllegalArgumentException.class)
	public void visitingBarFail(@Mocked final IBar input)
		throws Exception {

		Chains.builder().add(object).add(string).add(number).add(integer).add(foo).add(bar).add(foobar).visits(false).build().invoke(input);
	}


	@Test
	public void visitingFoobar(@Mocked final IFooBar input)
		throws Exception {

		new Expectations() {
			{
				object.invoke(any);
				times = 1;
				string.invoke(anyString);
				times = 0;
				number.invoke(anyDouble);
				times = 0;
				integer.invoke(anyInt);
				times = 0;
				foo.invoke(input);
				times = 1;
				bar.invoke(input);
				times = 1;
				foobar.invoke(input);
				times = 1;
			}
		};
		Chains.builder().add(object).add(string).add(number).add(integer).add(foo).add(bar).add(foobar).visits(true).build().invoke(input);
	}


	@Test(expectedExceptions = IllegalArgumentException.class)
	public void visitingFoobarFail(@Mocked final IFooBar input)
		throws Exception {

		Chains.builder().add(object).add(string).add(number).add(integer).add(foo).add(bar).add(foobar).visits(false).build().invoke(input);
	}


	/*
	 * 
	 * 
	 * 
	 */

	private interface IFoo {
	}


	private interface IBar {
	}


	private interface IFooBar
		extends IFoo, IBar {
	}
}
