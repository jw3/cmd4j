package cmd4j;

import java.util.UUID;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.Commands.ForwardingCommand;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand3;

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
	public void noReturn(@Mocked final ICommand1 forwarded)
		throws Exception {

		new Expectations() {
			{
				forwarded.invoke();
			}
		};

		Chains.create(new ForwardingCommand(forwarded)).invoke();
	}


	@Test
	public void returns(@Mocked final ICommand3<String> forwarded)
		throws Exception {

		final String expected = UUID.randomUUID().toString();
		new Expectations() {
			{
				forwarded.invoke();
				result = expected;
			}
		};

		final String actual = Chains.builder()//
			.add(new ForwardingCommand(forwarded))
			.returns(String.class)
			.build()
			.invoke();

		Assert.assertEquals(actual, expected);
	}
}
