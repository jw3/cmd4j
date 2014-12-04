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
	 * if an input is not specified the command will be invoked with null as the parameter
	 */
	@Test
	public void without()
		throws Exception {

		new Expectations() {
			{
				a.invoke(null);
				times = 1;
			}
		};
		Chains.create(a).invoke();
	}
}
