package cmd4j;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;
import cmd4j.testing.Services;

/**
 *
 *
 * @author wassj
 *
 */
public class ObservablesReturningTest {

	@Test
	public void testCommand()
		throws Exception {

		final String expected = UUID.randomUUID().toString().substring(0, 7);
		final TestVariable<Boolean> ran = TestVariable.create(false);
		final String result = Chains.create(Observers.observable(Does.returns(expected)).after(Does.set(ran, true))).invoke();

		ran.assertEquals(true);
		Assert.assertEquals(result, expected);
	}


	@Test
	public void testChain()
		throws Exception {

		final String expected = UUID.randomUUID().toString().substring(0, 7);
		final TestVariable<Boolean> ran = TestVariable.create(false);
		final String result = Observers.observable(Chains.create(Does.returns(expected))).after(Does.set(ran, true)).invoke();

		ran.assertEquals(true);
		Assert.assertEquals(result, expected);
	}


	@Test
	public void testSubmittedChain()
		throws Exception {

		final String expected = UUID.randomUUID().toString().substring(0, 7);
		final TestVariable<Boolean> ran = TestVariable.create(false);
		final IChain<String> chain = Observers.observable(Chains.create(Does.returns(expected))).after(Does.set(ran, true));
		final String result = Chains.submit(chain, Services.t1.executor()).get();

		ran.assertEquals(true);
		Assert.assertEquals(result, expected);
	}


	@Test
	public void testCallableChain()
		throws Exception {

		final String expected = UUID.randomUUID().toString().substring(0, 7);
		final TestVariable<Boolean> ran = TestVariable.create(false);
		final IChain<String> chain = Observers.observable(Chains.create(Does.returns(expected))).after(Does.set(ran, true));
		final Callable<String> callable = Commands.callable(chain);
		final String result = Services.t1.executor().submit(callable).get();

		ran.assertEquals(true);
		Assert.assertEquals(result, expected);
	}
}
