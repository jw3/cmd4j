package cmd4j;

import org.testng.annotations.Test;

import cmd4j.Chains;
import cmd4j.Executors2;
import cmd4j.IChain;
import cmd4j.testing.AssertThread;
import cmd4j.testing.Service;

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

		Chains.builder().add(AssertThread.isEDT()).executor(Service.edt.executor()).build().invoke();
	}


	@Test
	public void trivialSwitching()
		throws Exception {

		Chains.builder()
		//
			.add(AssertThread.isEDT())
			.executor(Service.edt.executor())
			//
			.add(AssertThread.is(Service.t1))
			.executor(Service.t1.executor())
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

		Chains.builder().add(AssertThread.isCurrent()).build().invoke();
	}


	/**
	 * test that an unthreaded chain will run unthreaded links on the caller thread (aka stripping the threading)
	 * @throws Exception
	 */
	@Test
	public void stripThreadingWithUnthreadedChain()
		throws Exception {

		Chains.builder()//
			.add(AssertThread.is(Service.t1))
			.executor(Service.t1.executor())

			.add(AssertThread.isCurrent())
			.executor(Service.edt.executor())

			.add(AssertThread.is(Service.t2))
			.executor(Service.t2.executor())

			.add(AssertThread.isCurrent())

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

		Chains.submit(Chains.builder().add(AssertThread.is(Service.t1)).build(), Service.t1.executor());
	}


	/**
	 * test that all commands with unspecified executors will run on the specified executor of the chain
	 * @throws Exception
	 */
	@Test
	public void unspecifiedRunsOnChainThread2()
		throws Exception {

		final IChain chain = Chains.builder()//
			.add(AssertThread.is(Service.t1))

			.add(AssertThread.isEDT())
			.executor(Service.edt.executor())

			.add(AssertThread.is(Service.t1))

			.build();
		Chains.submit(chain, Service.t1.executor());
	}


	@Test
	public void specifyChainsThreadToForceAllUnspecifiedCommandsOntoIt1()
		throws Exception {

		final IChain chain = Chains.builder()
		//
			.add(AssertThread.isCurrent())

			.add(AssertThread.isCurrent())

			.add(AssertThread.isCurrent())

			.build();
		Chains.submit(chain, Executors2.sameThreadExecutor());
	}


	@Test
	public void specifyChainsThreadToForceAllUnspecifiedCommandsOntoIt2()
		throws Exception {

		final IChain chain = Chains.builder()
		//
			.add(AssertThread.isEDT())

			.add(AssertThread.isEDT())

			.add(AssertThread.isEDT())

			.build();
		Chains.submit(chain, Service.edt.executor());
	}


	@Test
	public void specifyChainsThreadToForceAllUnspecifiedCommandsOntoIt3()
		throws Exception {

		final IChain chain = Chains.builder()
		//
			.add(AssertThread.is(Service.edt))

			.add(AssertThread.is(Service.edt))

			.add(AssertThread.is(Service.edt))

			.build();
		Chains.submit(chain, Service.edt.executor());
	}


	@Test
	public void specifyChainsThreadEnsureAllSpecifiedsAreNotForcedOntoIt()
		throws Exception {

		final IChain chain = Chains.builder()
		//
			.add(AssertThread.isCurrent())

			.add(AssertThread.isEDT())
			.executor(Service.edt.executor())

			.add(AssertThread.isCurrent())

			.add(AssertThread.is(Service.t1))
			.executor(Service.t1.executor())

			.build();
		Chains.submit(chain, Executors2.sameThreadExecutor());
	}
}
