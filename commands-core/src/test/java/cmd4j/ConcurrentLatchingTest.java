package cmd4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand1;
import cmd4j.testing.Services;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * validate command execution blocking using latches
 * @author wassj
 *
 */
public class ConcurrentLatchingTest {

	@Test
	public void test(@Mocked final ICommand1 a, @Mocked final ICommand1 b, @Mocked final ICommand1 c)
		throws Exception {

		// use a latch to block b until c is complete
		new Expectations() {
			{
				a.invoke();
				c.invoke();
				b.invoke();
			}
		};

		final CountDownLatch latch = new CountDownLatch(1);

		final IChain<Void> chain = Chains.builder().add(a).add(Commands.waitFor(latch)).add(b).build();

		// add a wait just to illustrate that b is actually stopped for a bit
		final IChain<Void> chain2 = Chains.builder().add(Commands.waitFor(100)).add(c).add(Commands.countDown(latch)).build();

		final Future<Void> f = Commands.submit(chain, Services.multi1.executor());
		Commands.submit(chain2, MoreExecutors.sameThreadExecutor());
		f.get();
	}
}
