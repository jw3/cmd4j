package cmd4j;

import java.util.UUID;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.IStateCommand.IStateCommand4M;

/**
 * {@link IStateCommand4M} validation
 * @author wassj
 *
 */
public class Commands4mStateTest {

	@Test
	public void returnsOther3(@Mocked final IStateCommand4M_1<String> state, @Mocked final ICommand3<String> other)
		throws Exception {

		final String input = UUID.randomUUID().toString();
		final String expected = UUID.randomUUID().toString();
		new Expectations() {
			{
				state.invoke(input);
				result = other;
				other.invoke();
				result = expected;
			}
		};

		final Object actual = Chains.create(state).invoke(new String[] {input});
		Assert.assertEquals(actual, expected);
	}


	private interface IStateCommand4M_1<T>
		extends IStateCommand4M<T> {
		IReturningCommand<T> invoke(String lhs);
	}
}
