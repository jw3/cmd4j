package cmd4j;

import java.util.concurrent.ExecutorService;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand1;
import cmd4j.testing.IService;
import cmd4j.testing.Says;
import cmd4j.testing.Says.ISayFactory;
import cmd4j.testing.Services;

/**
 * Test a variety of different {@link ExecutorService executor} usages
 * 
 * @author wassj
 *
 */
public class ConcurrentExecutorSpecificationTest {

	/**
	 * c2 is sandwiched and runs on different executor
	 */
	@Test
	public void test(@Mocked final ICommand1 c1, @Mocked final ICommand1 c2, @Mocked final ICommand1 c3)
		throws Exception {

		new Expectations() {
			{
				c1.invoke();
				c2.invoke();
				c3.invoke();
			}
		};

		Chains.builder() //
			.add(c1)
			.add(c2)
			.executor(Services.t1.executor())
			.add(c3)
			.build()
			.invoke();
	}


	@Test
	public void testUnspecifiedLinkExecutor()
		throws Exception {

		final ISayFactory say = Says.factory();
		Chains.builder().add(say.thread()).build().invoke();
		Assert.assertEquals(say.toString(), name());
	}


	@Test
	public void testLinkExecutor()
		throws Exception {

		final ISayFactory say = Says.factory();
		Chains.builder().add(say.thread()).executor(Services.t1.executor()).build().invoke();
		Assert.assertEquals(say.toString(), name(Services.t1));
	}


	@Test
	public void testLinkExecutorX2()
		throws Exception {

		final ISayFactory say = Says.factory();
		Chains.builder()//
			.add(say.thread())
			.executor(Services.t1.executor())

			.add(say.thread())
			.executor(Services.t1.executor())

			.build()
			.invoke();

		Assert.assertEquals(say.toString(), name(Services.t1, Services.t1));
	}


	@Test
	public void testDifferentLinkExecutorX2()
		throws Exception {

		final ISayFactory say = Says.factory();
		Chains.builder()//
			.add(say.thread())
			.executor(Services.t1.executor())

			.add(say.thread())
			.executor(Services.t2.executor())

			.build()
			.invoke();

		Assert.assertEquals(say.toString(), name(Services.t1, Services.t2));
	}


	@Test
	public void testChainExecutor()
		throws Exception {

		final ISayFactory say = Says.factory();
		final IChain<Void> chain = Chains.builder().add(say.thread()).build();
		Chains.submit(chain, Services.t1.executor()).get();
		Assert.assertEquals(say.toString(), name(Services.t1));
	}


	@Test
	public void testChainExecutorWithLinkOverride()
		throws Exception {

		final ISayFactory say = Says.factory();
		final IChain<Void> chain = Chains.builder()//
			.add(say.thread())
			//.executor(provided by the chain)

			.add(say.thread())
			.executor(Services.t2.executor())
			.build();

		Chains.submit(chain, Services.t1.executor()).get();
		Assert.assertEquals(say.toString(), name(Services.t1, Services.t2));
	}


	@Test
	public void testMultipleChains()
		throws Exception {

		final ISayFactory say = Says.factory();
		final IChain<Void> a = Chains.builder().add(say.thread()).build();
		final IChain<Void> b = Chains.builder().add(say.thread()).build();
		Chains.builder().add(a).add(b).build().invoke();

		Assert.assertEquals(say.toString(), name(Services.current(), Services.current()));
	}


	@Test
	public void testMultipleChainsMakeCompositeThreaded()
		throws Exception {

		final ISayFactory say = Says.factory();
		final IChain<Void> a = Chains.builder().add(say.thread()).build();
		final IChain<Void> b = Chains.builder().add(say.thread()).build();
		final IChain<Void> c = Chains.builder().add(a).add(b).build();

		Chains.submit(c, Services.t1.executor()).get();

		Assert.assertEquals(say.toString(), name(Services.t1, Services.t1));
	}


	@Test
	public void testMultipleChainsMakeCompositeThreadedWhileOverriddingOneLink()
		throws Exception {

		final ISayFactory say = Says.factory();
		final IChain<Void> a = Chains.builder().add(say.thread()).build();
		final IChain<Void> b = Chains.builder().add(say.thread()).executor(Services.t2.executor()).build();
		final IChain<Void> c = Chains.builder().add(a).add(b).build();

		Chains.submit(c, Services.t1.executor()).get();

		Assert.assertEquals(say.toString(), name(Services.t1, Services.t2));
	}


	@Test
	public void testMultipleChainsOverideOne()
		throws Exception {

		final ISayFactory say = Says.factory();
		final IChain<Void> a = Chains.builder().add(say.thread()).build();
		final IChain<Void> b = Chains.builder().add(say.thread()).build();
		Chains.builder().add(a).executor(Services.t1.executor()).add(b).build().invoke();

		Assert.assertEquals(say.toString(), name(Services.t1, Services.current()));
	}


	/*
	 * 
	 * 
	 * 
	 */

	private static String name(final IService... services) {
		final StringBuilder buffer = new StringBuilder();
		if (services.length > 0) {
			for (final IService service : services) {
				buffer.append(service.name());
			}
		}
		else {
			buffer.append(Services.current().name());
		}
		return buffer.toString();
	}
}
