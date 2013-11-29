package cmd4j;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.testing.Does;
import cmd4j.testing.Services;

/**
 *
 *
 * @author wassj
 *
 */
public class ConcurrentReturnValuesTest {

	@Test
	public void test()
		throws InterruptedException, ExecutionException {

		final String value = UUID.randomUUID().toString().substring(0, 6);
		final IChain<String> chain = Chains.create(Does.returns(value));

		final Future<String> future = Concurrency.submit(chain, Services.t1.executor());
		Assert.assertEquals(future.get(), value);
	}
}
