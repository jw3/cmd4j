package cmd4j;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommandCallback;
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
	public void onSuccess_1_1()
		throws Exception {

		final Variable<String> called = Variable.create();
		final String value = UUID.randomUUID().toString().substring(0, 6);

		final ICommand command = Commands.callback(Tests.returns(value), new ICommandCallback<String>() {
			public void onSuccess(final String returns) {
				called.setValue(returns);
			}


			public void onFailure(final Exception e) {
			}
		});

		Commands.execute(command);
		called.assertEquals(value);
	}


	@Test
	public void onSuccess_void()
		throws Exception {

		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Commands.callback(Commands.nop(), new ICommandCallback<Void>() {
			public void onSuccess(final Void returns) {
				called.setValue(true);
			}


			public void onFailure(final Exception e) {
			}
		});
		Commands.execute(command);
		called.assertEquals(true);
	}


	@Test
	public void onFailure_void() {
		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Commands.callback(Say.boom(), new ICommandCallback<Void>() {
			public void onSuccess(final Void returns) {
			}


			public void onFailure(final Exception e) {
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
