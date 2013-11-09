package cmd4j;

import java.util.UUID;

import org.testng.annotations.Test;

import cmd4j.testing.Does;
import cmd4j.testing.Does.Variable;

/**
 *
 *
 * @author wassj
 *
 */
public class ObservableCommandsTest {

	@Test
	public void onResults()
		throws Exception {

		final Variable<String> called = Variable.create();
		final String value = UUID.randomUUID().toString().substring(0, 6);

		final ICommand command = Observers.observable(Does.returns(value)).results(Does.set(called));

		Chains.create(command).invoke();
		called.assertEquals(value);
	}


	@Test
	public void onSuccess()
		throws Exception {

		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Observers.observable(Does.nothing()).onSuccess(Does.set(called, true));
		Chains.create(command).invoke();
		called.assertEquals(true);
	}


	@Test
	public void onFailure() {
		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Observers.observable(Does.boom()).onFailure(Does.set(called, true));
		try {
			Chains.create(command).invoke();
		}
		catch (Exception e) {
			// ignore
		}

		called.assertEquals(true);
	}


	@Test
	public void nesting1x()
		throws Exception {

		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Observers.observable(Does.nothing()).onSuccess(Does.set(called, true));
		final IChain<Void> chain = Chains.create(command);
		Chains.builder().add(chain).build().invoke();
		called.assertEquals(true);
	}


	@Test
	public void nesting2x()
		throws Exception {

		final Variable<Boolean> called = Variable.create(false);
		final ICommand command = Observers.observable(Does.nothing()).onSuccess(Does.set(called, true));
		final IChain<Void> chain = Chains.create(command);
		final IChain<Void> chain2 = Chains.builder().add(chain).build();
		Chains.builder().add(chain2).build().invoke();
		called.assertEquals(true);
	}


	@Test
	public void bothCommandAndChain()
		throws Exception {

		final Variable<Boolean> called = Variable.create(false);
		final Variable<Boolean> called2 = Variable.create(false);

		final ICommand command = Observers.observable(Does.nothing()).onSuccess(Does.set(called, true));
		final IChain<Void> chain = Observers.observable(Chains.create(command)).onSuccess(Does.set(called2, true));
		chain.invoke();

		called.assertEquals(true);
		called2.assertEquals(true);
	}
}
