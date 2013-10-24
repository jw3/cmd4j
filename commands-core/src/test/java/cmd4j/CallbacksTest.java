package cmd4j;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.testng.annotations.Test;

import cmd4j.testing.Say;
import cmd4j.testing.Tests;
import cmd4j.testing.Tests.Variable;

/**
 *
 *
 * @author wassj
 *
 */
public class CallbacksTest {

	@Test
	public void onResults()
		throws Exception {

		final Variable<String> called = Variable.create();
		final String value = UUID.randomUUID().toString().substring(0, 6);

		final ICommand command = Commands.observable(Tests.returns(value)).results(Tests.set(called));

		Commands.execute(command);
		called.assertEquals(value);
	}


	@Test
	public void onSuccess()
		throws Exception {

		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Commands.observable(Commands.nop()).onSuccess(Tests.set(called, true));
		Commands.execute(command);
		called.assertEquals(true);
	}


	@Test
	public void onFailure() {
		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Commands.observable(Say.boom()).onFailure(Tests.set(called, true));
		try {
			Commands.execute(command);
		}
		catch (Exception e) {
			Assert.assertTrue(e instanceof ExecutionException);
		}

		called.assertEquals(true);
	}
}
