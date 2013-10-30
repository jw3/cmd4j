package cmd4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.testing.Asserts;
import cmd4j.testing.Does;
import cmd4j.testing.Does.Variable;
import cmd4j.testing.IService;
import cmd4j.testing.Services;
import cmd4j.testing.Services.Mode;

/**
 * A test of thread blocking scenarios
 * 
 * 1) blockMain: Block the main thread
 * 2) blockMain_runOnExecutor: Block the main thread, running the chain on an executor
 * 3) blockExecutorThread: Block an executor thread (invoke() within callable)
 * 4) blockExecutorThread_runOnDifferentExecutor: Block an executor thread (invoke() within callable) running the chain on a different executor
 * 5) blockExecutorThread_runOnSameExecutor_SingleThread: Block an executor thread (invoke() within callable) running the chain on the same executor
 * 		- results in a double block call that is not reentrant on the executor thread.
 * 6) blockExecutorThread_runOnSameExecutor_Pooled: Same as #5 using a pooled executor which alleviates the deadlock problem
 * 
 * @author wassj
 *
 */
public class ThreadBlockingScenariosTest {

	// #1
	@Test
	public void blockMain()
		throws Exception {

		final Variable<Boolean> var = new Variable<Boolean>();
		Chains.builder()//
			.add(Asserts.is(Services.t1))
			.add(Does.set(var, true))
			.build()
			.invoke();

		var.assertEquals(true);
	}


	// #2
	@Test
	public void blockMain_runOnExecutor()
		throws Exception {

		final Variable<Boolean> var = new Variable<Boolean>();
		final IChain<Void> chain = Chains.builder()//
			.add(Asserts.is(Services.t1))
			.add(Does.set(var, true))
			.build();
		Chains.submit(chain, Services.t1.executor()).get();

		var.assertEquals(true);
	}


	// #3
	@Test
	public void blockExecutorThread()
		throws Exception {

		final Variable<Boolean> var = new Variable<Boolean>();
		final Callable<Void> runChain = new Callable<Void>() {
			public Void call()
				throws Exception {

				Chains.builder()//
					.add(Asserts.is(Services.t1))
					.add(Does.set(var, true))
					.build()
					.invoke();

				Assert.assertEquals(var.getValue(), Boolean.TRUE);

				return null;
			}
		};

		Services.t1.executor().submit(runChain).get();
		var.assertEquals(true);
	}


	// #4
	@Test
	public void blockExecutorThread_runOnDifferentExecutor()
		throws Exception {

		final Variable<Boolean> var = new Variable<Boolean>();
		final Callable<Void> runChain = new Callable<Void>() {
			public Void call()
				throws Exception {

				final IChain<Void> chain = Chains.builder()//
					.add(Asserts.is(Services.t1))
					.add(Does.set(var, true))
					.build();
				Chains.submit(chain, Services.t1.executor()).get();

				Assert.assertEquals(var.getValue(), Boolean.TRUE);

				return null;
			}
		};

		Services.t2.executor().submit(runChain).get();
		var.assertEquals(true);
	}


	/**
	 * #5
	 * expected failure: dont do this!!!
	 */
	@Test(expectedExceptions = TimeoutException.class)
	public void blockExecutorThread_runOnSameExecutor_SingleThread()
		throws Exception {

		// use a throwaway service here so the exception doesnt blow up any other tests
		final IService service = Services.create("blockExecutorThread_runOnSameExecutor_SingleThread", Mode.SINGLE);

		final Variable<Boolean> var = new Variable<Boolean>();
		final Callable<Void> runChain = new Callable<Void>() {
			public Void call()
				throws Exception {

				final IChain<Void> chain = Chains.builder()//
					.add(Asserts.is(service))
					.add(Does.set(var, true))
					.build();
				Chains.submit(chain, service.executor()).get();

				return null;
			}
		};

		service.executor().submit(runChain).get(1, TimeUnit.SECONDS);
	}


	/**
	 * #6
	 * modification of #5 to use a pooled executor, which fixes the deadlock problem because there are multiple threads
	 */
	@Test
	public void blockExecutorThread_runOnSameExecutor_Pooled()
		throws Exception {

		final Variable<Boolean> var = new Variable<Boolean>();
		final Callable<Void> runChain = new Callable<Void>() {
			public Void call()
				throws Exception {

				final IChain<Void> chain = Chains.builder()//
					.add(Asserts.is(Services.t1))
					.add(Does.set(var, true))
					.build();
				Chains.submit(chain, Services.multi1.executor()).get();

				return null;
			}
		};

		Services.multi1.executor().submit(runChain).get();
		var.assertEquals(true);
	}
}
