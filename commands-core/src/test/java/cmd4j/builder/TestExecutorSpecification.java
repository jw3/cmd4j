package cmd4j.builder;

import java.util.concurrent.ExecutorService;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.IChain;
import cmd4j.common.Chains;
import cmd4j.testing.IService;
import cmd4j.testing.Say;
import cmd4j.testing.Service;
import cmd4j.testing.Say.ISayFactory;

/**
 * Test a variety of different {@link ExecutorService executor} usages
 * 
 * @author wassj
 *
 */
public class TestExecutorSpecification {

	@Test
	public void test()
		throws Exception {
		Chains.builder().add(Say.what(1)).add(Say.what("...")).executor(Service.t1.executor()).add(Say.what(2)).build().invoke("mississippi");
	}


	@Test
	public void testUnspecifiedLinkExecutor()
		throws Exception {

		final ISayFactory say = Say.factory();
		Chains.builder().add(say.thread()).build().invoke();
		Assert.assertEquals(say.toString(), name());
	}


	@Test
	public void testLinkExecutor()
		throws Exception {

		final ISayFactory say = Say.factory();
		Chains.builder().add(say.thread()).executor(Service.t1.executor()).build().invoke();
		Assert.assertEquals(say.toString(), name(Service.t1));
	}


	@Test
	public void testLinkExecutorX2()
		throws Exception {

		final ISayFactory say = Say.factory();
		Chains.builder()//
			.add(say.thread())
			.executor(Service.t1.executor())

			.add(say.thread())
			.executor(Service.t1.executor())

			.build()
			.invoke();

		Assert.assertEquals(say.toString(), name(Service.t1, Service.t1));
	}


	@Test
	public void testDifferentLinkExecutorX2()
		throws Exception {

		final ISayFactory say = Say.factory();
		Chains.builder()//
			.add(say.thread())
			.executor(Service.t1.executor())

			.add(say.thread())
			.executor(Service.t2.executor())

			.build()
			.invoke();

		Assert.assertEquals(say.toString(), name(Service.t1, Service.t2));
	}


	@Test
	public void testChainExecutor()
		throws Exception {

		final ISayFactory say = Say.factory();
		Chains.makeThreaded(Chains.builder().add(say.thread()).build(), Service.t1.executor()).invoke();
		Assert.assertEquals(say.toString(), name(Service.t1));
	}


	@Test
	public void testChainExecutorWithLinkOverride()
		throws Exception {

		final ISayFactory say = Say.factory();
		final IChain chain = Chains.builder()//
			.add(say.thread())
			//.executor(provided by the chain)

			.add(say.thread())
			.executor(Service.t2.executor())
			.build();

		Chains.makeThreaded(chain, Service.t1.executor()).invoke();
		Assert.assertEquals(say.toString(), name(Service.t1, Service.t2));
	}


	@Test
	public void testMultipleChains()
		throws Exception {

		final ISayFactory say = Say.factory();
		final IChain a = Chains.builder().add(say.thread()).build();
		final IChain b = Chains.builder().add(say.thread()).build();
		Chains.builder().add(a).add(b).build().invoke();

		Assert.assertEquals(say.toString(), name(Service.current(), Service.current()));
	}


	@Test
	public void testMultipleChainsMakeCompositeThreaded()
		throws Exception {

		final ISayFactory say = Say.factory();
		final IChain a = Chains.builder().add(say.thread()).build();
		final IChain b = Chains.builder().add(say.thread()).build();
		final IChain c = Chains.builder().add(a).add(b).build();

		Chains.makeThreaded(c, Service.t1.executor()).invoke();

		Assert.assertEquals(say.toString(), name(Service.t1, Service.t1));
	}


	@Test
	public void testMultipleChainsMakeCompositeThreadedWhileOverriddingOneLink()
		throws Exception {

		final ISayFactory say = Say.factory();
		final IChain a = Chains.builder().add(say.thread()).build();
		final IChain b = Chains.builder().add(say.thread()).executor(Service.t2.executor()).build();
		final IChain c = Chains.builder().add(a).add(b).build();

		Chains.makeThreaded(c, Service.t1.executor()).invoke();

		Assert.assertEquals(say.toString(), name(Service.t1, Service.t2));
	}


	@Test
	public void testMultipleChainsOverideOne()
		throws Exception {

		final ISayFactory say = Say.factory();
		final IChain a = Chains.builder().add(say.thread()).build();
		final IChain b = Chains.builder().add(say.thread()).build();
		Chains.builder().add(a).executor(Service.t1.executor()).add(b).build().invoke();

		Assert.assertEquals(say.toString(), name(Service.t1, Service.current()));
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
			buffer.append(Service.current().name());
		}
		return buffer.toString();
	}
}
