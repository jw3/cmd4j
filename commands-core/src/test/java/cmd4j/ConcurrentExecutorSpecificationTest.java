package cmd4j;

import java.util.concurrent.ExecutorService;

import org.testng.Assert;
import org.testng.annotations.Test;

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

	@Test
	public void test()
		throws Exception {
		Chains.builder().add(Says.what(1)).add(Says.what("...")).executor(Services.t1.executor()).add(Says.what(2)).build().invoke("mississippi");
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
		Concurrency.submit(chain, Services.t1.executor()).get();
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

		Concurrency.submit(chain, Services.t1.executor()).get();
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

		Concurrency.submit(c, Services.t1.executor()).get();

		Assert.assertEquals(say.toString(), name(Services.t1, Services.t1));
	}


	@Test
	public void testMultipleChainsMakeCompositeThreadedWhileOverriddingOneLink()
		throws Exception {

		final ISayFactory say = Says.factory();
		final IChain<Void> a = Chains.builder().add(say.thread()).build();
		final IChain<Void> b = Chains.builder().add(say.thread()).executor(Services.t2.executor()).build();
		final IChain<Void> c = Chains.builder().add(a).add(b).build();

		Concurrency.submit(c, Services.t1.executor()).get();

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
