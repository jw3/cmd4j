package cmd4j;

import java.util.UUID;

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

		final ICommand command = Observers.observable(Does.returns(value)).results(Does.set(called));

		Commands.invoke(command);
		called.assertEquals(value);
	}


	@Test
	public void onSuccess()
		throws Exception {

		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Observers.observable(Commands.nop()).onSuccess(Does.set(called, true));
		Commands.invoke(command);
		called.assertEquals(true);
	}


	@Test
	public void onFailure() {
		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Observers.observable(Says.boom()).onFailure(Does.set(called, true));
		try {
			Commands.invoke(command);
		}
		catch (Exception e) {
			// ignore
		}

		called.assertEquals(true);
	}
}
