package cmd4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.testng.annotations.Test;

import cmd4j.testing.Say;
import cmd4j.testing.Service;

/**
 *
 *
 * @author wassj
 *
 */
public class LatchedCommandTest {

	@Test
	public void firstTest()
		throws Exception {

		final CountDownLatch latch = new CountDownLatch(1);
		final StringBuilder buffer = new StringBuilder();

		final IChain chain = Chains.builder().add(Say.what("0", buffer)).add(Commands.waitFor(latch)).add(Say.what("2", buffer)).build();
		final IChain chain2 = Chains.builder().add(Commands.waitFor(1000)).add(Say.what("1", buffer)).add(Commands.countDown(latch)).build();
		final Future<Void> f = Chains.submit(chain, Service.multi1.executor());
		Chains.submit(chain2, Service.multi1.executor());
		f.get();

		final String result = buffer.toString();
		Assert.assertFalse(result.isEmpty());

		// not planning on double digit results strings, will revisit if needed
		Assert.assertTrue(result.length() < 10);

		for (int i = 0; i < result.length(); ++i) {
			Assert.assertEquals(String.valueOf(i), String.valueOf(result.charAt(i)));
		}
	}
}
