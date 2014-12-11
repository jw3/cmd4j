package cmd4j;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2;

/**
 * validate the behavior of a chained input command if no input to the chain is specified
 * @author wassj
 *
 */
public class InputCommandsWithoutInputTest {
	@Mocked
	private ICommand2<Integer> a;


	/**
	 * if an input is specified the command will be invoked as expected
	 */
	@Test
	public void with()
		throws Exception {

		new Expectations() {
			{
				a.invoke(1);
				times = 1;
			}
		};
		Chains.create(a).invoke(1);
	}


	/**
	 * if a non nullable input command is specified, it will not be invoked without input
	 */
	@Test
	public void without()
		throws Exception {

		new Expectations() {
			{
				a.invoke(null);
				times = 0;
			}
		};
		Chains.create(a).invoke();
	}


	// need to figure out how to mock this test
	public void nullableWithout() {
		// same as without, but using an ICommand2 that has @Nullable on the parameter
		// execution times would == 1 on this test
	}
}
