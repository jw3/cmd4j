package cmd4j.builder;

import org.junit.Assert;
import org.testng.annotations.Test;

import cmd4j.IChain;
import cmd4j.ICommand1;
import cmd4j.Say;
import cmd4j.common.Chains;

/**
 * Test the {@link IDoneCallback} functionality
 *
 * @author wassj
 *
 */
public class CallbackTest {

	@Test
	public void testWhenDoneSuccessHandler()
		throws Exception {

		final boolean[] val = new boolean[1];
		Assert.assertFalse(val[0]);

		final IChain chain = Chains.onSuccess(Chains.empty(), new ICommand1() {
			public void invoke() {
				val[0] = true;
			}
		});
		chain.invoke();

		Assert.assertTrue(val[0]);
	}


	@Test
	public void testWhenDoneExceptionHandler()
		throws Exception {

		final boolean[] val = new boolean[1];
		Assert.assertFalse(val[0]);

		final IChain chain = Chains.onFailure(Chains.builder().add(Say.boom()).build(), new ICommand1() {
			public void invoke() {
				val[0] = true;
			}
		});
		chain.invoke();

		Assert.assertTrue(val[0]);
	}
}
