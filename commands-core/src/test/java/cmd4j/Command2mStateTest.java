package cmd4j;

import java.util.UUID;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.IStateCommand.IStateCommand2M;

/**
 * {@link IStateCommand2M} validation
 * @author wassj
 *
 */
public class Command2mStateTest {

	@Test
	public void returnsOther1(@Mocked final IStateCommand2M_1 state, @Mocked final ICommand1 other)
		throws Exception {

		final String input = UUID.randomUUID().toString();
		new Expectations() {
			{
				state.invoke(input);
				result = other;
				other.invoke();
			}
		};

		Chains.create(state).invoke(new String[] {input});
	}


	private interface IStateCommand2M_1
		extends IStateCommand2M {
		ICommand invoke(String lhs);
	}
}
