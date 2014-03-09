package cmd4j;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.Commands.ForwardingCommand;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;

/**
 *
 * @author wassj
 *
 */
public class ForwardingCommandTest {

	@Test
	public void empty()
		throws Exception {

		final ForwardingCommand command = new ForwardingCommand();
		Chains.create(command).invoke();
	}


	@Test(expectedExceptions = IllegalStateException.class)
	public void self()
		throws Exception {

		final ForwardingCommand command = new ForwardingCommand();
		command.setCommand(command);
		Chains.create(command).invoke();
	}


	@Test
	public void noReturn()
		throws Exception {

		final TestVariable<Boolean> var = TestVariable.create(false);
		Chains.create(new ForwardingCommand(Does.toggle(var))).invoke();
		var.assertEquals(true);
	}


	@Test
	public void returns()
		throws Exception {

		final String expected = UUID.randomUUID().toString();
		final String actual = Chains.builder().add(new ForwardingCommand(Commands.returns(expected))).returns(String.class).build().invoke();
		Assert.assertEquals(actual, expected);
	}
}
