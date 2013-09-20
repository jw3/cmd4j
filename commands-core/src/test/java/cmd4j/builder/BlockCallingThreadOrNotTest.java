package cmd4j.builder;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.AssertThread;
import cmd4j.IService;
import cmd4j.Service;
import cmd4j.Service.Mode;
import cmd4j.Tests;
import cmd4j.Tests.Variable;
import cmd4j.common.ChainBuilder;

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
public class BlockCallingThreadOrNotTest {

	// #1
	@Test
	public void blockMain()
		throws Exception {

		final Variable<Boolean> var = new Variable<Boolean>();
		ChainBuilder.create()//
			.add(AssertThread.is(Service.t1))
			.add(Tests.set(var, true))
			.build()
			.invoke();

		Assert.assertEquals(var.value(), Boolean.TRUE);
	}


	// #2
	@Test
	public void blockMain_runOnExecutor()
		throws Exception {

		final Variable<Boolean> var = new Variable<Boolean>();
		ChainBuilder.create()//
			.add(AssertThread.is(Service.t1))
			.add(Tests.set(var, true))
			.build(Service.t1.executor())
			.invoke();

		Assert.assertEquals(var.value(), Boolean.TRUE);
	}


	// #3
	@Test
	public void blockExecutorThread()
		throws Exception {

		final Variable<Boolean> var = new Variable<Boolean>();
		final Callable<Void> runChain = new Callable<Void>() {
			public Void call()
				throws Exception {

				ChainBuilder.create()//
					.add(AssertThread.is(Service.t1))
					.add(Tests.set(var, true))
					.build()
					.invoke();

				Assert.assertEquals(var.value(), Boolean.TRUE);

				return null;
			}
		};

		Service.t1.executor().submit(runChain).get();
		Assert.assertEquals(var.value(), Boolean.TRUE);
	}


	// #4
	@Test
	public void blockExecutorThread_runOnDifferentExecutor()
		throws Exception {

		final Variable<Boolean> var = new Variable<Boolean>();
		final Callable<Void> runChain = new Callable<Void>() {
			public Void call()
				throws Exception {

				ChainBuilder.create()//
					.add(AssertThread.is(Service.t1))
					.add(Tests.set(var, true))
					.build(Service.t1.executor())
					.invoke();

				Assert.assertEquals(var.value(), Boolean.TRUE);

				return null;
			}
		};

		Service.t2.executor().submit(runChain).get();
		Assert.assertEquals(var.value(), Boolean.TRUE);
	}


	/**
	 * #5
	 * expected failure: dont do this!!!
	 */
	@Test(expectedExceptions = TimeoutException.class)
	public void blockExecutorThread_runOnSameExecutor_SingleThread()
		throws Exception {

		// use a throwaway service here so the exception doesnt blow up any other tests
		final IService service = Service.create("blockExecutorThread_runOnSameExecutor_SingleThread", Mode.SINGLE);

		final Variable<Boolean> var = new Variable<Boolean>();
		final Callable<Void> runChain = new Callable<Void>() {
			public Void call()
				throws Exception {

				ChainBuilder.create()//
					.add(AssertThread.is(service))
					.add(Tests.set(var, true))
					.build(service.executor())
					.invoke();

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

				ChainBuilder.create()//
					.add(AssertThread.is(Service.t1))
					.add(Tests.set(var, true))
					.build(Service.multi10.executor())
					.invoke();

				return null;
			}
		};

		Service.multi10.executor().submit(runChain).get();
		Assert.assertEquals(var.value(), Boolean.TRUE);
	}
}
