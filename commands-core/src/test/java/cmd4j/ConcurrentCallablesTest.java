package cmd4j;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.testing.Asserts;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;
import cmd4j.testing.Services;

import com.google.common.base.Predicate;

/**
 *
 *
 * @author wassj
 *
 */
public class ConcurrentCallablesTest {

	@Test
	public void wrapCallable()
		throws Exception {

		final TestVariable<Integer> var = new TestVariable<Integer>(0);
		Chains.builder() //
			.add(Asserts.isEquals(var, 0))
			.add(Commands.from(new IncrementVariable(var)))
			.add(Asserts.isEquals(var, 1))
			.add(Commands.from(new IncrementVariable(var)))
			.add(Asserts.isEquals(var, 2))
			.build()
			.invoke();
	}


	@Test
	public void wrapCommand()
		throws Exception {

		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			{
				final String expected = UUID.randomUUID().toString().substring(0, 7);
				final String actual = executor.submit(Commands.callable(Does.returns(expected))).get();
				Assert.assertEquals(actual, expected);
			}
			{
				final TestVariable<String> var = TestVariable.create(null);
				final String expected = UUID.randomUUID().toString().substring(0, 7);
				executor.submit(Commands.callable(Does.set(var), expected)).get();
				Assert.assertEquals(var.get(), expected);
			}
			{
				final TestVariable<String> var = TestVariable.create(null);
				final String expected = UUID.randomUUID().toString().substring(0, 7);
				executor.submit(Commands.runnable(Does.set(var, expected))).get();
				Assert.assertEquals(var.get(), expected);
			}
		}
		finally {
			Chains.create(Commands.shutdownExecutor(executor), Asserts.predicate(new IsShutdown())).invoke(executor);
		}
	}


	@Test
	public void wrapChain()
		throws Exception {

		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			{
				final String expected = UUID.randomUUID().toString().substring(0, 7);
				final String actual = executor.submit(Chains.callable(Chains.create(Does.returns(expected)))).get();
				Assert.assertEquals(actual, expected);
			}
			{
				final TestVariable<String> var = TestVariable.create(null);
				final String expected = UUID.randomUUID().toString().substring(0, 7);
				executor.submit(Chains.callable(Chains.create(Does.set(var)), expected)).get();
				Assert.assertEquals(var.get(), expected);
			}
		}
		finally {
			Chains.create(Commands.shutdownExecutor(executor), Asserts.predicate(new IsShutdown())).invoke(executor);
		}
	}


	@Test
	public void wrapLongRunning()
		throws Exception {

		final TestVariable<Integer> var = new TestVariable<Integer>(0);
		Chains.builder() //
			.add(Asserts.isEquals(var, 0))

			.add(Commands.from(Services.t1.executor().submit(new IncrementVariable(var, 100))))
			.add(Asserts.isEquals(var, 1))

			.add(Commands.from(Services.t2.executor().submit(new IncrementVariable(var, 200))))
			.add(Asserts.isEquals(var, 2))

			.add(Commands.from(Services.t2.executor().submit(new IncrementVariable(var, 300))))
			.add(Asserts.isEquals(var, 3))

			.build()
			.invoke();
	}


	@Test
	public void submitCommand()
		throws Exception {

		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			{
				final String expected = UUID.randomUUID().toString().substring(0, 7);
				final String actual = Commands.submit(Does.returns(expected), executor).get();
				Assert.assertEquals(actual, expected);
			}
			{
				final TestVariable<String> var = TestVariable.create(null);
				final String expected = UUID.randomUUID().toString().substring(0, 7);
				Commands.submit(Does.set(var), expected, executor).get();
				Assert.assertEquals(var.get(), expected);
			}
		}
		finally {
			Chains.create(Commands.shutdownExecutor(executor), Asserts.predicate(new IsShutdown())).invoke(executor);
		}
	}


	static class IsShutdown
		implements Predicate<ExecutorService> {
		public boolean apply(final ExecutorService input) {
			return input.isShutdown();
		}
	}


	static class IncrementVariable
		implements Callable<Void> {

		private final TestVariable<Integer> var;
		private final long delay;


		public IncrementVariable(final TestVariable<Integer> var) {
			this(var, 0);
		}


		public IncrementVariable(final TestVariable<Integer> var, final long delay) {
			this.var = var;
			this.delay = delay;
		}


		public Void call()
			throws Exception {

			Thread.sleep(delay);
			var.set(var.get() + 1);

			return null;
		}
	}
}
