package cmd4j;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.testng.annotations.Test;

import cmd4j.testing.Does;
import cmd4j.testing.Does.Variable;
import cmd4j.testing.Says;

/**
 *
 *
 * @author wassj
 *
 */
public class CallbackCommandTest {

	@Test
	public void onResults()
		throws Exception {

		final Variable<String> called = Variable.create();
		final String value = UUID.randomUUID().toString().substring(0, 6);

		final ICommand command = Commands.observable(Does.returns(value)).results(Does.set(called));

		Commands.execute(command);
		called.assertEquals(value);
	}


	@Test
	public void onSuccess()
		throws Exception {

		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Commands.observable(Commands.nop()).onSuccess(Does.set(called, true));
		Commands.execute(command);
		called.assertEquals(true);
	}


	@Test
	public void onFailure() {
		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Commands.observable(Says.boom()).onFailure(Does.set(called, true));
		try {
			Commands.execute(command);
		}
		catch (Exception e) {
			Assert.assertTrue(e instanceof ExecutionException);
		}

		called.assertEquals(true);
	}
}
