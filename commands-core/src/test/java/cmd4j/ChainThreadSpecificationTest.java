package cmd4j;

import org.testng.annotations.Test;

import cmd4j.testing.Asserts;
import cmd4j.testing.Services;

/**
 *
 *
 * @author wassj
 *
 */
public class ChainThreadSpecificationTest {

	/**
	 * test running a single command on the EDT
	 * @throws Exception
	 */
	@Test
	public void trivialEdtExecutionTest()
		throws Exception {

		Chains.builder().add(Asserts.isEDT()).executor(Services.edt.executor()).build().invoke();
	}


	@Test
	public void trivialSwitching()
		throws Exception {

		Chains.builder()
		//
			.add(Asserts.isEDT())
			.executor(Services.edt.executor())
			//
			.add(Asserts.is(Services.t1))
			.executor(Services.t1.executor())
			//
			.build()
			.invoke();
	}


	/**
	 * test that an unthreaded chain will run unthreaded links on the caller thread
	 * @throws Exception
	 */
	@Test
	public void unspecifiedExecutorChainRunsOnSameThread()
		throws Exception {

		Chains.builder().add(Asserts.isCurrent()).build().invoke();
	}


	/**
	 * test that an unthreaded chain will run unthreaded links on the caller thread (aka stripping the threading)
	 * @throws Exception
	 */
	@Test
	public void stripThreadingWithUnthreadedChain()
		throws Exception {

		Chains.builder()//
			.add(Asserts.is(Services.t1))
			.executor(Services.t1.executor())

			.add(Asserts.isCurrent())
			.executor(Services.edt.executor())

			.add(Asserts.is(Services.t2))
			.executor(Services.t2.executor())

			.add(Asserts.isCurrent())

			.build()
			.invoke();
	}


	/**
	 * Test that if a chain has a thread specified any unspecified commands will run on that thread
	 * @throws Exception
	 */
	@Test
	public void unspecifiedRunsOnChainThread1()
		throws Exception {

		Chains.submit(Chains.builder().add(Asserts.is(Services.t1)).build(), Services.t1.executor());
	}


	/**
	 * test that all commands with unspecified executors will run on the specified executor of the chain
	 * @throws Exception
	 */
	@Test
	public void unspecifiedRunsOnChainThread2()
		throws Exception {

		final IChain<Void> chain = Chains.builder()//
			.add(Asserts.is(Services.t1))

			.add(Asserts.isEDT())
			.executor(Services.edt.executor())

			.add(Asserts.is(Services.t1))

			.build();
		Chains.submit(chain, Services.t1.executor());
	}


	@Test
	public void specifyChainsThreadToForceAllUnspecifiedCommandsOntoIt1()
		throws Exception {

		final IChain<Void> chain = Chains.builder()
		//
			.add(Asserts.isCurrent())

			.add(Asserts.isCurrent())

			.add(Asserts.isCurrent())

			.build();
		Chains.submit(chain, Executors2.sameThreadExecutor());
	}


	@Test
	public void specifyChainsThreadToForceAllUnspecifiedCommandsOntoIt2()
		throws Exception {

		final IChain<Void> chain = Chains.builder()
		//
			.add(Asserts.isEDT())

			.add(Asserts.isEDT())

			.add(Asserts.isEDT())

			.build();
		Chains.submit(chain, Services.edt.executor());
	}


	@Test
	public void specifyChainsThreadToForceAllUnspecifiedCommandsOntoIt3()
		throws Exception {

		final IChain<Void> chain = Chains.builder()
		//
			.add(Asserts.is(Services.edt))

			.add(Asserts.is(Services.edt))

			.add(Asserts.is(Services.edt))

			.build();
		Chains.submit(chain, Services.edt.executor());
	}


	@Test
	public void specifyChainsThreadEnsureAllSpecifiedsAreNotForcedOntoIt()
		throws Exception {

		final IChain<Void> chain = Chains.builder()
		//
			.add(Asserts.isCurrent())

			.add(Asserts.isEDT())
			.executor(Services.edt.executor())

			.add(Asserts.isCurrent())

			.add(Asserts.is(Services.t1))
			.executor(Services.t1.executor())

			.build();
		Chains.submit(chain, Executors2.sameThreadExecutor());
	}
}
