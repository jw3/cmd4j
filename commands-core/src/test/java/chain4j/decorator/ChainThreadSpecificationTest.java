package chain4j.decorator;

import org.testng.annotations.Test;

import chain4j.AssertThread;
import chain4j.AssertableThreadFactory;
import chain4j.Service;
import chain4j.builder.ChainBuilder;

import com.google.common.util.concurrent.MoreExecutors;

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

		ChainBuilder.create().add(AssertThread.isEDT()).executor(Service.edt.get()).build().invoke();
	}


	@Test
	public void trivialSwitching()
		throws Exception {

		final AssertableThreadFactory atf = AssertableThreadFactory.create();

		ChainBuilder.create()
		//
			.add(AssertThread.isEDT())
			.executor(Service.edt.get())
			//
			.add(atf.assertThis())
			.executor(Service.wrap(atf))
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

		final AssertableThreadFactory atf = AssertableThreadFactory.create();
		ChainBuilder.create()//
			.add(AssertThread.isCurrent())
			.executor(Service.wrap(atf))

			.add(AssertThread.isCurrent())
			.executor(Service.edt.get())

			.add(AssertThread.isCurrent())
			.executor(Service.a.get())

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

		final AssertableThreadFactory atf = AssertableThreadFactory.create();
		ChainBuilder.create().add(atf.assertThis()).build(Service.wrap(atf)).invoke();
	}


	/**
	 * test that all commands with unspecified executors will run on the specified executor of the chain
	 * @throws Exception
	 */
	@Test
	public void unspecifiedRunsOnChainThread2()
		throws Exception {

		final AssertableThreadFactory atf = AssertableThreadFactory.create();
		ChainBuilder.create()//
			.add(atf.assertThis())

			.add(AssertThread.isEDT())
			.executor(Service.edt.get())

			.add(atf.assertThis())

			.build(Service.wrap(atf))
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

			.build(MoreExecutors.sameThreadExecutor())
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

			.build(Service.edt.get())
			.invoke();
	}


	@Test
	public void specifyChainsThreadToForceAllUnspecifiedCommandsOntoIt3()
		throws Exception {

		final AssertableThreadFactory atf = AssertableThreadFactory.create();
		ChainBuilder.create()
		//
			.add(atf.assertThis())

			.add(atf.assertThis())

			.add(atf.assertThis())

			.build(Service.wrap(atf))
			.invoke();
	}


	@Test
	public void specifyChainsThreadEnsureAllSpecifiedsAreNotForcedOntoIt()
		throws Exception {

		final AssertableThreadFactory atf = AssertableThreadFactory.create();

		ChainBuilder.create()
		//
			.add(AssertThread.isCurrent())

			.add(AssertThread.isEDT())
			.executor(Service.edt.get())

			.add(AssertThread.isCurrent())

			.add(atf.assertThis())
			.executor(Service.wrap(atf))

			.build(MoreExecutors.sameThreadExecutor())
			.invoke();
	}
}
