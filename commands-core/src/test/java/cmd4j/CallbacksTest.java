package cmd4j;

import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommandCallback;
import cmd4j.testing.Say;
import cmd4j.testing.Tests.Variable;

/**
 *
 *
 * @author wassj
 *
 */
public class CallbacksTest {

	@Test
	public void onSuccess()
		throws Exception {

		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Commands.callback(Commands.nop(), new ICommandCallback() {
			public void onSuccess() {
				called.setValue(true);
			}


			public void onFailure() {
			}
		});
		Commands.execute(command);
		called.assertEquals(true);
	}


	@Test
	public void onFailure() {
		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Commands.callback(Say.boom(), new ICommandCallback() {
			public void onSuccess() {
			}


			public void onFailure() {
				called.setValue(true);
			}
		});
		try {
			Commands.execute(command);
		}
		catch (Exception e) {
			Assert.assertTrue(e instanceof ExecutionException);
		}

		called.assertEquals(true);
	}
}
