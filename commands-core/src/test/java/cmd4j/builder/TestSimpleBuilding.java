package cmd4j.builder;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.common.Chains;
import cmd4j.testing.Say;
import cmd4j.testing.Say.ISayFactory;
import cmd4j.testing.Service;

/**
 * @author wassj
 *
 */
public class TestSimpleBuilding {

	@Test
	public void firstTest()
		throws Exception {

		Chains.builder().add(Say.what("hello")).executor(Service.edt.executor()).add(Say.what("world")).executor(Service.t2.executor()).add(Say.what("!")).executor(Service.t1.executor()).build().invoke();
	}


	@Test
	public void building1()
		throws Exception {

		final ISayFactory say = Say.factory();
		Chains.builder().add(say.what("success")).build().invoke();
		Assert.assertEquals(say.toString(), "success");
	}


	@Test
	public void building2()
		throws Exception {

		final ISayFactory say = Say.factory();
		Chains.builder().add(say.what("succ")).add(say.what("ess")).build().invoke();
		Assert.assertEquals(say.toString(), "success");
	}


	@Test
	public void buildingOnDifferentExecutors()
		throws Exception {

		final ISayFactory say = Say.factory();
		Chains.builder()//
			.add(say.what("1"))
			.executor(Service.t1.executor())

			.add(say.what("2"))
			.executor(Service.t2.executor())

			.add(say.what("3"))
			.executor(Service.multi1.executor())

			.add(say.what("4"))
			.executor(Service.edt.executor())

			.build()
			.invoke();

		Assert.assertEquals(say.toString(), "1234");
	}
}
