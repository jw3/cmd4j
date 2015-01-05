package cmd4j;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand3;

import com.google.common.base.Predicates;

/**
 * validate run until with repeats enabled
 * 
 * REVISIT the tests were written but the function hasnt been implemented yet
 * 
 * @author wassj
 *
 */
public class RunUntil2Tests {
	private static final boolean enabled = false;


	/**
	 * validate that a command that doesnt match doesnt complete
	 */
	@Test(timeOut = 1000, enabled = enabled)
	public void repeats__neverDone() {
		Chains.create(Commands.until(Commands.returns(false), Predicates.alwaysTrue()));
		Assert.fail("should not have completed");
	}


	@Test(enabled = enabled)
	public void repeats__firstIterationSatisfies(@Mocked final ICommand3<Integer> c1)
		throws Exception {

		new Expectations() {
			{
				c1.invoke();
				result = 1;
			}
		};

		Chains.create(Commands.until(c1, Predicates.equalTo(1))).invoke();
	}


	@Test(enabled = enabled)
	public void repeats__secondIterationSatisfies(@Mocked final ICommand3<Integer> c1)
		throws Exception {

		// run the command two times
		new Expectations() {
			{
				c1.invoke();
				result = 0;
				c1.invoke();
				result = 1;
			}
		};

		Chains.create(Commands.until(c1, Predicates.equalTo(1))).invoke();
	}
}
