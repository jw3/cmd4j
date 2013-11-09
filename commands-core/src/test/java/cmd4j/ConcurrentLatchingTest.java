package cmd4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.testng.annotations.Test;

import cmd4j.testing.Says;
import cmd4j.testing.Services;

/**
 *
 *
 * @author wassj
 *
 */
public class ConcurrentLatchingTest {

	@Test
	public void firstTest()
		throws Exception {

		final CountDownLatch latch = new CountDownLatch(1);
		final StringBuilder buffer = new StringBuilder();

		final IChain<Void> chain = Chains.builder().add(Says.what("0", buffer)).add(Concurrent.waitFor(latch)).add(Says.what("2", buffer)).build();
		final IChain<Void> chain2 = Chains.builder().add(Concurrent.waitFor(100)).add(Says.what("1", buffer)).add(Concurrent.countDown(latch)).build();
		final Future<Void> f = Concurrent.submit(chain, Services.multi1.executor());
		Concurrent.submit(chain2, Services.multi1.executor());
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
