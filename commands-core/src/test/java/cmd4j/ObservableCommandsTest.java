package cmd4j;

import java.util.UUID;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2;
import cmd4j.testing.Asserts;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;

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

		final TestVariable<String> called = TestVariable.create();
		final String value = UUID.randomUUID().toString().substring(0, 6);

		final ICommand command = Observers.observable(Does.returns(value)).results(Does.set(called));

		Chains.create(command).invoke();
		called.assertEquals(value);
	}


	@Test
	public void onSuccess()
		throws Exception {

		final TestVariable<Boolean> called = TestVariable.create(false);
		final ICommand command = Observers.observable(Does.nothing()).onSuccess(Does.set(called, true));
		Chains.create(command).invoke();
		called.assertEquals(true);
	}


	@Test
	public void onFailure() {
		final TestVariable<Boolean> called = TestVariable.create(false);
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

		final TestVariable<Boolean> called = TestVariable.create(false);
		final ICommand command = Observers.observable(Does.nothing()).onSuccess(Does.set(called, true));
		final IChain<Void> chain = Chains.create(command);
		Chains.builder().add(chain).build().invoke();
		called.assertEquals(true);
	}


	@Test
	public void nesting2x()
		throws Exception {

		final TestVariable<Boolean> called = TestVariable.create(false);
		final ICommand command = Observers.observable(Does.nothing()).onSuccess(Does.set(called, true));
		final IChain<Void> chain = Chains.create(command);
		final IChain<Void> chain2 = Chains.builder().add(chain).build();
		Chains.builder().add(chain2).build().invoke();
		called.assertEquals(true);
	}


	@Test
	public void bothCommandAndChain()
		throws Exception {

		final TestVariable<Boolean> called = TestVariable.create(false);
		final TestVariable<Boolean> called2 = TestVariable.create(false);

		final ICommand command = Observers.observable(Does.nothing()).onSuccess(Does.set(called, true));
		final IChain<Void> chain = Observers.observable(Chains.create(command)).onSuccess(Does.set(called2, true));
		chain.invoke();

		called.assertEquals(true);
		called2.assertEquals(true);
	}


	@Test
	public void results()
		throws Exception {

		{
			final int value = 101010;
			final ICommand command = Observers.observable(Does.returns(value)).results(Asserts.is(value));
			Chains.builder().add(command).build().invoke();
		}
		{
			final String value = UUID.randomUUID().toString().substring(0, 7);
			final ICommand command = Observers.observable(Does.returns(value)).results(Asserts.is(value));
			Chains.builder().add(command).build().invoke();
		}
	}


	@Test
	public void resultsDtoMismatch()
		throws Exception {

		final TestVariable<Boolean> fits = TestVariable.create(false);
		final ICommand command = Observers.observable(Does.returns(true)).results(Does.set(fits), new ICommand2<Integer>() {
			public void invoke(final Integer input)
				throws Exception {
				throw new Exception("Dto does not fit, this should not have been run");
			}
		});
		Chains.builder().add(command).build().invoke();

		fits.assertEquals(true);
	}
}
