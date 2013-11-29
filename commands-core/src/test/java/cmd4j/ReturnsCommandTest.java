package cmd4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.Commands.Variable;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.IOutputPipe;
import cmd4j.testing.Does.TestVariable;

/**
 * verify the behavior of the returns methods in {@link Commands}
 *
 * @author wassj
 *
 */
public class ReturnsCommandTest {

	@Test
	public void testReturnValue()
		throws Exception {

		final Object expected = new Object();
		final Object actual = Chains.create(Commands.returns(expected)).invoke();
		Assert.assertEquals(actual, expected);
	}


	@Test
	public void testReturnIntoVariable()
		throws Exception {

		final TestVariable<Integer> result = TestVariable.create();
		final Integer expected = Integer.MAX_VALUE;
		/*final Object chainReturn =*/Chains.create(Commands.returns(expected), integerResult(result)).invoke();
		result.assertEquals(expected);

		/**
		 * ideally we can assert null on the Void Chain, but due to a defect we cannot
		 * @see ApiHoles#chainsAlwaysReturn()
		 */
		//Assert.assertNull(chainReturn);
	}


	@Test
	public void testReturnIntoVariableThatDoesNotFit()
		throws Exception {

		final TestVariable<Integer> result = TestVariable.create();
		/*final Object chainReturn =*/Chains.visits(Chains.create(Commands.returns("string"), integerResult(result))).invoke();
		Assert.assertTrue(result.isNull());

		/**
		 * ideally we can assert null on the Void Chain, but due to a defect we cannot
		 * @see ApiHoles#chainsAlwaysReturn()
		 */
		//Assert.assertNull(chainReturn);
	}


	/**
	 * example of piping a value out through a variable
	 * @param result
	 * @return
	 */
	public static ICommand integerResult(final Variable<Integer> result) {
		class pipe
			implements ICommand2<Integer>, IOutputPipe<Integer> {
			public void invoke(final Integer input) {
				result.set(input);
			}
		}
		return new pipe();
	}
}
