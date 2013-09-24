package cmd4j.builder;

import org.testng.annotations.Test;

import cmd4j.AssertThread;
import cmd4j.Service;
import cmd4j.common.ChainBuilder;
import cmd4j.common.CmdExecutors;

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

		ChainBuilder.create().add(AssertThread.isEDT()).executor(Service.edt.executor()).build().invoke();
	}


	@Test
	public void trivialSwitching()
		throws Exception {

		ChainBuilder.create()
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
	public void unthreadedChainRunsOnSameThread()
		throws Exception {

		ChainBuilder.create().add(AssertThread.isCurrent()).buildUnthreaded().invoke();
	}


	/**
	 * test that an unthreaded chain will run unthreaded links on the caller thread (aka stripping the threading)
	 * @throws Exception
	 */
	@Test
	public void stripThreadingWithUnthreadedChain()
		throws Exception {

		ChainBuilder.create()//
			.add(AssertThread.is(Service.t1))
			.executor(Service.t1.executor())

			.add(AssertThread.isCurrent())
			.executor(Service.edt.executor())

			.add(AssertThread.is(Service.t2))
			.executor(Service.t2.executor())

			.add(AssertThread.isCurrent())

			.buildUnthreaded()
			.invoke();
	}


	/**
	 * Test that if a chain has a thread specified any unspecified commands will run on that thread
	 * @throws Exception
	 */
	@Test
	public void unspecifiedRunsOnChainThread1()
		throws Exception {

		ChainBuilder.create().add(AssertThread.is(Service.t1)).build(Service.t1.executor()).invoke();
	}


	/**
	 * test that all commands with unspecified executors will run on the specified executor of the chain
	 * @throws Exception
	 */
	@Test
	public void unspecifiedRunsOnChainThread2()
		throws Exception {

		ChainBuilder.create()//
			.add(AssertThread.is(Service.t1))

			.add(AssertThread.isEDT())
			.executor(Service.edt.executor())

			.add(AssertThread.is(Service.t1))

			.build(Service.t1.executor())
			.invoke();
	}


	@Test
	public void specifyChainsThreadToForceAllUnspecifiedCommandsOntoIt1()
		throws Exception {

		ChainBuilder.create()
		//
			.add(AssertThread.isCurrent())

			.add(AssertThread.isCurrent())

			.add(AssertThread.isCurrent())

			.build(CmdExecutors.sameThreadExecutor())
			.invoke();
	}


	@Test
	public void specifyChainsThreadToForceAllUnspecifiedCommandsOntoIt2()
		throws Exception {

		ChainBuilder.create()
		//
			.add(AssertThread.isEDT())

			.add(AssertThread.isEDT())

			.add(AssertThread.isEDT())

			.build(Service.edt.executor())
			.invoke();
	}


	@Test
	public void specifyChainsThreadToForceAllUnspecifiedCommandsOntoIt3()
		throws Exception {

		ChainBuilder.create()
		//
			.add(AssertThread.is(Service.edt))

			.add(AssertThread.is(Service.edt))

			.add(AssertThread.is(Service.edt))

			.build(Service.edt.executor())
			.invoke();
	}


	@Test
	public void specifyChainsThreadEnsureAllSpecifiedsAreNotForcedOntoIt()
		throws Exception {

		ChainBuilder.create()
		//
			.add(AssertThread.isCurrent())

			.add(AssertThread.isEDT())
			.executor(Service.edt.executor())

			.add(AssertThread.isCurrent())

			.add(AssertThread.is(Service.t1))
			.executor(Service.t1.executor())

			.build(CmdExecutors.sameThreadExecutor())
			.invoke();
	}
}
