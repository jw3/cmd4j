package cmd4j;

import static cmd4j.testing.Tests.is;
import static cmd4j.testing.Tests.toggle;
import static cmd4j.testing.Tests.var;

import org.testng.annotations.Test;

import cmd4j.Chains;
import cmd4j.IChain;
import cmd4j.testing.Say;
import cmd4j.testing.Tests.Variable;

/**
 * Test the {@link IDoneCallback} functionality
 *
 * @author wassj
 *
 */
public class TestChainCallbacks {

	@Test
	public void testOnSuccessHandler()
		throws Exception {

		final Variable<Boolean> v = var(false);
		Chains.observable(Chains.empty()).onSuccess(toggle(v)).invoke();
		v.assertEquals(true);
	}


	@Test
	public void testOnSuccessHandlerWithFailure() {
		final Variable<Boolean> v = var(false);
		final IChain chain = Chains.observable(Chains.create(Say.boom())).onSuccess(toggle(v));
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
		final IChain chain = Chains.observable(Chains.builder().add(Say.boom()).build()).onFailure(toggle(v));
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
		Chains.observable(Chains.empty()).onFailure(toggle(v)).invoke();
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
		Chains.observable(Chains.builder().add(is(v, true)).build()).before(toggle(v)).invoke();
	}


	/**
	 * the chain does not toggle the value but it is different since we 
	 * toggle it in an after handler
	 */
	@Test
	public void testAfter()
		throws Exception {

		final Variable<Boolean> v = var(false);
		Chains.observable(Chains.builder().add(is(v, false)).build()).after(toggle(v)).invoke();
		v.assertEquals(true);
	}


	/**
	 * the chain does not toggle the value but it is different since we 
	 * toggle it in an after handler
	 */
	@Test
	public void testAfterWithFailure() {
		final Variable<Boolean> v = var(false);
		final IChain chain = Chains.observable(Chains.builder().add(is(v, false)).add(Say.boom()).build()).after(toggle(v));
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
}
