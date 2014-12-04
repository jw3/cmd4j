package cmd4j;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2;

/**
 * Validate the visiting decorator installed with {@link Chains#visits(IChain)}
 *
 * @author wassj
 *
 */
public class VisitingDecoratorTest {
	@Mocked
	private ICommand2<Integer> integer;


	/*
	 * basic expected behvaior on dto value 
	 */
	@Test
	public void match()
		throws Exception {

		final Integer expected = Integer.MAX_VALUE;
		new Expectations() {
			{
				integer.invoke(expected);
				times = 1;
			}
		};
		Chains.create(integer).invoke(expected);
	}


	/*
	 * basic expected behvaior on dto value mismatch
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void mismatch()
		throws Exception {

		Chains.create(integer).invoke("not an integer");
	}


	/*
	 * basic expected behvaior on dto value mismatch with a visitor decoration
	 */
	@Test
	public void mismatchVisitDecoration()
		throws Exception {

		final Integer expected = Integer.MAX_VALUE;
		new Expectations() {
			{
				integer.invoke(expected);
				times = 0;
			}
		};
		Chains.visits(Chains.create(integer)).invoke("not an integer");
	}


	/*
	 * basic expected behvaior on dto value mismatch with a builder an implicit unset visitor flag
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void mismatchNonVisitChainBuilder_implicit()
		throws Exception {

		Chains.builder().add(integer).build().invoke("not an integer");
	}


	/*
	 * basic expected behvaior on dto value mismatch with a builder an explicit unset visitor flag
	 */
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void mismatchNonVisitChainBuilder_explicit()
		throws Exception {

		Chains.builder().add(integer).visits(false).build().invoke("not an integer");
	}


	/*
	 * basic expected behvaior on dto value mismatch with builder set visitor flag
	 */
	@Test
	public void mismatchVisitChainBuilder()
		throws Exception {

		new Expectations() {
			{
				integer.invoke(anyInt);
				times = 0;
			}
		};
		Chains.builder().add(integer).visits(true).build().invoke("not an integer");
	}
}
