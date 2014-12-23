package cmd4j;

import java.util.UUID;

import mockit.Mocked;
import mockit.Verifications;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2M;

/**
 *
 * @author wassj
 *
 */
public class Command2mTest {

	@Test
	public void singleMatching(@Mocked final ICommand2M_1 command)
		throws Exception {

		final String lhs = UUID.randomUUID().toString();
		Chains.create(command).invoke(new Object[] {lhs});

		new Verifications() {
			{
				command.invoke(lhs);
				times = 1;
			}
		};
	}


	@Test
	public void singleNonMatching(@Mocked final ICommand2M_1 command)
		throws Exception {

		Chains.create(command).invoke(new Object[] {1});

		new Verifications() {
			{
				command.invoke((String)any);
				times = 0;
			}
		};
	}


	@Test
	public void twoArg_sameType_matching(@Mocked final ICommand2M_2 command)
		throws Exception {

		final String lhs = UUID.randomUUID().toString();
		final String rhs = UUID.randomUUID().toString();
		Chains.create(command).invoke(new Object[] {lhs, rhs});

		new Verifications() {
			{
				command.invoke(lhs, rhs);
				times = 1;
			}
		};
	}


	@Test
	public void twoArg_sameType_null(@Mocked final ICommand2M_2 command)
		throws Exception {

		final String lhs = UUID.randomUUID().toString();
		Chains.create(command).invoke(new Object[] {lhs, null});

		new Verifications() {
			{
				command.invoke(lhs, (String)any);
				times = 0;
			}
		};
	}


	@Test
	public void twoArg_diffType_matching(@Mocked final ICommand2M_3 command)
		throws Exception {

		final String lhs = UUID.randomUUID().toString();
		final Integer rhs = 10001;
		Chains.create(command).invoke(new Object[] {lhs, rhs});

		new Verifications() {
			{
				command.invoke(lhs, rhs);
				times = 1;
			}
		};
	}


	@Test
	public void twoArg_diffType_nomatch(@Mocked final ICommand2M_3 command)
		throws Exception {

		final String lhs = UUID.randomUUID().toString();
		final Integer rhs = 10001;

		// lhs and rhs swapped
		Chains.create(command).invoke(new Object[] {rhs, lhs});

		new Verifications() {
			{
				command.invoke(lhs, rhs);
				times = 0;
			}
		};
	}


	private interface ICommand2M_1
		extends ICommand2M {
		void invoke(String lhs);
	}


	private interface ICommand2M_2
		extends ICommand2M {
		void invoke(String lhs, String rhs);
	}


	private interface ICommand2M_3
		extends ICommand2M {
		void invoke(String lhs, Integer rhs);
	}
}
