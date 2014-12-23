package cmd4j;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand4M;

/**
 *
 * @author wassj
 *
 */
public class Command4mTest {

	@Test
	public void singleMatching(@Mocked final ICommand4M_1 command)
		throws Exception {

		final Integer expected = 1001;
		new Expectations() {
			{
				command.invoke(expected);
				result = expected;
				times = 1;
			}
		};
		final Integer actual = Chains.create(command).invoke(new Object[] {expected});
		Assert.assertEquals(actual, expected);
	}


	@Test
	public void twoArg_matching(@Mocked final ICommand4M_2 command)
		throws Exception {

		final Integer lhs = 1;
		final Integer rhs = 2;
		new Expectations() {
			{
				command.invoke(lhs, rhs);
				result = lhs + rhs;
				times = 1;
			}
		};
		final Integer actual = Chains.create(command).invoke(new Object[] {lhs, rhs});
		Assert.assertEquals(actual, Integer.valueOf(lhs + rhs));
	}


	private interface ICommand4M_1
		extends ICommand4M<Integer> {
		Integer invoke(Integer a);
	}


	private interface ICommand4M_2
		extends ICommand4M<Integer> {
		Integer invoke(Integer a, Integer b);
	}
}
