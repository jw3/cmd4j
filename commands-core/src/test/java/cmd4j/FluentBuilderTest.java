package cmd4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.IFunction;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;

/**
 * Testing the chain builder and Chains utils methods that rely on the chain builder
 *
 * @author wassj
 *
 */
public class FluentBuilderTest {

	/*
	 * verify that the chain builder returns will return the correct object when
	 * only a single returning command is present and no return function is used
	 */
	@Test
	public void testSingleCommand()
		throws Exception {

		final Object expected = new Object();
		final IChain<Object> chain = Chains.builder().add(Does.returns(expected)).returns().build();
		final Object actual = chain.invoke();
		Assert.assertEquals(actual, expected);
	}


	@Test
	public void testSingleCommandWithUtils()
		throws Exception {

		final Object expected = new Object();
		final IChain<Object> chain = Chains.returns(Chains.builder().add(Does.returns(expected)).build());
		final Object actual = chain.invoke();
		Assert.assertEquals(actual, expected);
	}


	/*
	 * verify that the chain builder returns will return the correct object when
	 * multiple returning commands are present and no return function is used
	 */
	@Test
	public void testMultipleCommands()
		throws Exception {

		final Object expected = new Object();
		final IChain<Object> chain = Chains.builder().add(Does.returns(new Object())).add(Does.returns(expected)).returns().build();
		final Object actual = chain.invoke();
		Assert.assertEquals(actual, expected);
	}


	@Test
	public void testMultipleCommandsWithUtils()
		throws Exception {

		final Object expected = new Object();
		final IChain<Object> chain = Chains.returns(Chains.builder().add(Does.returns(new Object())).add(Does.returns(expected)).build());
		final Object actual = chain.invoke();
		Assert.assertEquals(actual, expected);
	}


	/*
	 * verify that the chain builder returns will return the correct object when
	 * multiple returning commands are present and no return function is used
	 */
	@Test
	public void testClassSpecifiedConversion()
		throws Exception {

		final Object expected = "expected";
		final IChain<String> chain = Chains.builder().add(Does.returns(expected)).returns(String.class).build();
		final String actual = chain.invoke();
		Assert.assertEquals(actual, expected);
	}


	@Test
	public void testClassSpecifiedConversionWithUtils()
		throws Exception {

		final Object expected = "expected";
		final IChain<String> chain = Chains.returns(Chains.builder().add(Does.returns(expected)).build(), String.class);
		final String actual = chain.invoke();
		Assert.assertEquals(actual, expected);
	}


	/*
	 * verify that the chain builder returns will return the correct object when
	 * multiple returning commands are present and no return function is used
	 */
	@Test
	public void testClassSpecifiedConversionDoesNotFit()
		throws Exception {

		final Object expected = true;
		final IChain<String> chain = Chains.builder().add(Does.returns(expected)).returns(String.class).build();
		final String actual = chain.invoke();
		Assert.assertNull(actual);
	}


	@Test
	public void testClassSpecifiedConversionDoesNotFitWithUtils()
		throws Exception {

		final Object expected = true;
		final IChain<String> chain = Chains.returns(Chains.builder().add(Does.returns(expected)).build(), String.class);
		final String actual = chain.invoke();
		Assert.assertNull(actual);
	}


	/*
	 * verify that a return function returns the correct value
	 */
	@Test
	public void testPassthroughReturnFunction()
		throws Exception {

		final TestVariable<Boolean> ran = TestVariable.create(false);
		final Object expected = new Object();
		final IChain<Object> chain = Chains.builder().add(Does.returns(expected)).returns(passthrough(ran)).build();
		final Object actual = chain.invoke();
		Assert.assertEquals(actual, expected);
		ran.assertEquals(true);
	}


	@Test
	public void testPassthroughReturnFunctionWithUtils()
		throws Exception {

		final TestVariable<Boolean> ran = TestVariable.create(false);
		final Object expected = new Object();
		final IChain<Object> chain = Chains.returns(Chains.builder().add(Does.returns(expected)).build(), passthrough(ran));
		final Object actual = chain.invoke();
		Assert.assertEquals(actual, expected);
		ran.assertEquals(true);
	}


	/*
	 * verify that a transform return function returns the correct value
	 */
	@Test
	public void testTransformReturnFunction()
		throws Exception {

		{
			final TestVariable<Boolean> ran = TestVariable.create(false);
			final Object expected = "expected";
			final IChain<String> chain = Chains.builder().add(Does.returns(expected)).returns(stringify(ran)).build();
			final Object actual = chain.invoke();
			Assert.assertEquals(actual, expected);
			ran.assertEquals(true);
		}
		{
			final TestVariable<Boolean> ran = TestVariable.create(false);
			final Object expected = 100;
			final IChain<String> chain = Chains.builder().add(Does.returns(expected)).returns(stringify(ran)).build();
			final Object actual = chain.invoke();
			Assert.assertEquals(actual, String.valueOf(expected));
			ran.assertEquals(true);
		}
	}


	@Test
	public void testTransformReturnFunctionWithUtils()
		throws Exception {

		{
			final TestVariable<Boolean> ran = TestVariable.create(false);
			final Object expected = "expected";
			final IChain<String> chain = Chains.returns(Chains.builder().add(Does.returns(expected)).build(), stringify(ran));
			final Object actual = chain.invoke();
			Assert.assertEquals(actual, expected);
			ran.assertEquals(true);
		}
		{
			final TestVariable<Boolean> ran = TestVariable.create(false);
			final Object expected = 100;
			final IChain<String> chain = Chains.returns(Chains.builder().add(Does.returns(expected)).build(), stringify(ran));
			final Object actual = chain.invoke();
			Assert.assertEquals(actual, String.valueOf(expected));
			ran.assertEquals(true);
		}
	}


	/*
	 * verify that a transform return function can nullify the return value
	 * there is no real use for this, but it is a simple result transform test
	 */
	@Test
	public void testNullifyReturnFunction()
		throws Exception {

		final TestVariable<Boolean> ran1 = TestVariable.create(false);
		IFunction<Object, String> fn = stringify(ran1);
		{
			final Object expected = "expected";
			final IChain<String> chain = Chains.builder().add(Does.returns(expected)).returns(fn).build();
			final Object actual = chain.invoke();
			Assert.assertEquals(actual, expected);
			ran1.assertEquals(true);
		}
		// now with nullify
		final TestVariable<Boolean> ran2 = TestVariable.create(false);
		fn = nullify(ran2);
		{
			final Object expected = "expected";
			final IChain<String> chain = Chains.builder().add(Does.returns(expected)).returns(fn).build();
			final Object actual = chain.invoke();
			Assert.assertNull(actual);
			ran2.assertEquals(true);
		}
	}


	@Test
	public void testNullifyReturnFunctionWithUtils()
		throws Exception {

		final TestVariable<Boolean> ran1 = TestVariable.create(false);
		IFunction<Object, String> fn = stringify(ran1);
		{
			final Object expected = "expected";
			final IChain<String> chain = Chains.returns(Chains.builder().add(Does.returns(expected)).build(), fn);
			final Object actual = chain.invoke();
			Assert.assertEquals(actual, expected);
			ran1.assertEquals(true);
		}
		// now with nullify
		final TestVariable<Boolean> ran2 = TestVariable.create(false);
		fn = nullify(ran2);
		{
			final Object expected = "expected";
			final IChain<String> chain = Chains.returns(Chains.builder().add(Does.returns(expected)).build(), fn);
			final Object actual = chain.invoke();
			Assert.assertNull(actual);
			ran2.assertEquals(true);
		}
	}


	/*
	 * verify that a transform return function returns null when the result does not fit the function
	 */
	@Test
	public void testTransformReturnFunctionDoesNotFit()
		throws Exception {

		{
			final TestVariable<Boolean> ran = TestVariable.create(false);
			final Object expected = "expected";
			final IChain<String> chain = Chains.builder().add(Does.returns(expected)).returns(stringifyNumber(ran)).build();
			final Object actual = chain.invoke();
			Assert.assertNull(actual);
			ran.assertEquals(false);
		}
		{
			final TestVariable<Boolean> ran = TestVariable.create(false);
			final Object expected = new Object();
			final IChain<String> chain = Chains.builder().add(Does.returns(expected)).returns(stringifyNumber(ran)).build();
			final Object actual = chain.invoke();
			Assert.assertNull(actual);
			ran.assertEquals(false);
		}
	}


	@Test
	public void testTransformReturnFunctionDoesNotFitWithUtils()
		throws Exception {

		{
			final TestVariable<Boolean> ran = TestVariable.create(false);
			final Object expected = "expected";
			final IChain<String> chain = Chains.returns(Chains.builder().add(Does.returns(expected)).build(), stringifyNumber(ran));
			final Object actual = chain.invoke();
			Assert.assertNull(actual);
			ran.assertEquals(false);
		}
		{
			final TestVariable<Boolean> ran = TestVariable.create(false);
			final Object expected = new Object();
			final IChain<String> chain = Chains.returns(Chains.builder().add(Does.returns(expected)).build(), stringifyNumber(ran));
			final Object actual = chain.invoke();
			Assert.assertNull(actual);
			ran.assertEquals(false);
		}
	}


	/*
	 * return the input object
	 */
	private IFunction<Object, Object> passthrough(final TestVariable<Boolean> ran) {
		return new IFunction<Object, Object>() {
			public Object invoke(final Object input) {
				ran.set(true);
				return input;
			}
		};
	}


	/*
	 * convert to string
	 */
	private IFunction<Object, String> stringify(final TestVariable<Boolean> ran) {
		return new IFunction<Object, String>() {
			public String invoke(final Object input) {
				ran.set(true);
				return String.valueOf(input);
			}
		};
	}


	/*
	 * convert to string
	 */
	private IFunction<Number, String> stringifyNumber(final TestVariable<Boolean> ran) {
		return new IFunction<Number, String>() {
			public String invoke(final Number input) {
				ran.set(true);
				return String.valueOf(input);
			}
		};
	}


	/*
	 * return null
	 */
	private IFunction<Object, String> nullify(final TestVariable<Boolean> ran) {
		return new IFunction<Object, String>() {
			public String invoke(final Object input) {
				ran.set(true);
				return null;
			}
		};
	}
}
