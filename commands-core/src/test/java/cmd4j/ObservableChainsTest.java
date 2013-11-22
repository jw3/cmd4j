package cmd4j;

import static cmd4j.testing.Asserts.is;
import static cmd4j.testing.Does.toggle;
import static cmd4j.testing.Does.var;

import java.util.UUID;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2;
import cmd4j.testing.Asserts;
import cmd4j.testing.Does;
import cmd4j.testing.Does.Variable;

/**
 * Test the {@link IDoneCallback} functionality
 *
 * @author wassj
 *
 */
public class ObservableChainsTest {

	@Test
	public void testOnSuccessHandler()
		throws Exception {

		final Variable<Boolean> v = var(false);
		Observers.observable(Chains.create()).onSuccess(toggle(v)).invoke();
		v.assertEquals(true);
	}


	@Test
	public void testOnSuccessHandlerWithFailure() {
		final Variable<Boolean> v = var(false);
		final IChain<Void> chain = Observers.observable(Chains.create(Does.boom())).onSuccess(toggle(v));
		try {
			chain.invoke();
		}
		catch (Exception e) {
			// expected
		}
		finally {
			v.assertEquals(false);
		}
	}


	@Test
	public void testOnFailureHandler() {
		final Variable<Boolean> v = var(false);
		final IChain<Void> chain = Observers.observable(Chains.builder().add(Does.boom()).build()).onFailure(toggle(v));
		try {
			chain.invoke();
		}
		catch (Exception e) {
			// expected
		}
		finally {
			v.assertEquals(true);
		}
	}


	@Test
	public void testOnFailureHandlerWithSuccess()
		throws Exception {

		final Variable<Boolean> v = var(false);
		Observers.observable(Chains.create()).onFailure(toggle(v)).invoke();
		v.assertEquals(false);
	}


	/**
	 * an assert that the value is different in the first link succeeds because we
	 * toggle the value in a before handler
	 */
	@Test
	public void testBefore()
		throws Exception {

		final Variable<Boolean> v = var(false);
		Observers.observable(Chains.builder().add(is(v, true)).build()).before(toggle(v)).invoke();
	}


	/**
	 * the chain does not toggle the value but it is different since we 
	 * toggle it in an after handler
	 */
	@Test
	public void testAfter()
		throws Exception {

		final Variable<Boolean> v = var(false);
		Observers.observable(Chains.builder().add(is(v, false)).build()).after(toggle(v)).invoke();
		v.assertEquals(true);
	}


	/**
	 * the chain does not toggle the value but it is different since we 
	 * toggle it in an after handler
	 */
	@Test
	public void testAfterWithFailure() {
		final Variable<Boolean> v = var(false);
		final IChain<Void> chain = Observers.observable(Chains.builder().add(is(v, false)).add(Does.boom()).build()).after(toggle(v));
		try {
			chain.invoke();
		}
		catch (Exception e) {
			// expected
		}
		finally {
			v.assertEquals(true);
		}
	}


	@Test
	public void results()
		throws Exception {

		{
			final int value = 101010;
			Observers.observable(Chains.builder().add(Does.returns(value)).build()).results(Asserts.is(value)).invoke();
		}
		{
			final String value = UUID.randomUUID().toString().substring(0, 7);
			Observers.observable(Chains.builder().add(Does.returns(value)).build()).results(Asserts.is(value)).invoke();
		}
	}


	@Test
	public void resultsDtoMismatch()
		throws Exception {

		final Variable<Boolean> fits = Variable.create(false);
		Observers.observable(Chains.builder().add(Does.returns(true)).build()).results(Does.set(fits), new ICommand2<Integer>() {
			public void invoke(final Integer input)
				throws Exception {
				throw new Exception("Dto does not fit, this should not have been run");
			}
		}).invoke();

		fits.assertEquals(true);
	}
}
