package cmd4j;

import junit.framework.Assert;

import org.testng.annotations.Test;

import cmd4j.Commands.Variable;
import cmd4j.ICommand.ICommand2;
import cmd4j.testing.Does.TestVariable;

/**
 * some simpler visiting tests using the chain decorator
 *
 * @author wassj
 *
 */
public class VisitingTest2 {

	/*
	 * basic expected behvaior on dto value 
	 */
	@Test
	public void match()
		throws Exception {

		final Integer expected = Integer.MAX_VALUE;
		final TestVariable<Integer> var = TestVariable.create();
		Chains.create(integerCommand(var)).invoke(expected);
		var.assertEquals(expected);
	}


	/*
	 * basic expected behvaior on dto value mismatch
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void mismatch()
		throws Exception {

		final TestVariable<Integer> var = TestVariable.create();
		Chains.create(integerCommand(var)).invoke("not an integer");
	}


	/*
	 * basic expected behvaior on dto value mismatch with a visitor decoration
	 */
	@Test
	public void mismatchVisitDecoration()
		throws Exception {

		final TestVariable<Integer> var = TestVariable.create();
		Chains.visits(Chains.create(integerCommand(var))).invoke("not an integer");
		Assert.assertTrue(var.isNull());
	}


	/*
	 * basic expected behvaior on dto value mismatch with a builder an implicit unset visitor flag
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void mismatchNonVisitChainBuilder_implicit()
		throws Exception {

		final TestVariable<Integer> var = TestVariable.create();
		Chains.builder().add(integerCommand(var)).build().invoke("not an integer");
		Assert.assertTrue(var.isNull());
	}


	/*
	 * basic expected behvaior on dto value mismatch with a builder an explicit unset visitor flag
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void mismatchNonVisitChainBuilder_explicit()
		throws Exception {

		final TestVariable<Integer> var = TestVariable.create();
		Chains.builder().add(integerCommand(var)).visits(false).build().invoke("not an integer");
		Assert.assertTrue(var.isNull());
	}


	/*
	 * basic expected behvaior on dto value mismatch with builder set visitor flag
	 */
	@Test
	public void mismatchVisitChainBuilder()
		throws Exception {

		final TestVariable<Integer> var = TestVariable.create();
		Chains.builder().add(integerCommand(var)).visits(true).build().invoke("not an integer");
		Assert.assertTrue(var.isNull());
	}


	public static ICommand integerCommand(final Variable<Integer> result) {
		return new ICommand2<Integer>() {
			public void invoke(final Integer input) {
				result.set(input);
			}
		};
	}
}
