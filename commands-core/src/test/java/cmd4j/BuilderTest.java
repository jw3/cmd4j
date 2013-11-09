package cmd4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.testing.Says;
import cmd4j.testing.Says.ISayFactory;
import cmd4j.testing.Services;

/**
 * @author wassj
 *
 */
public class BuilderTest {

	@Test
	public void firstTest()
		throws Exception {

		Chains.builder().add(Says.what("hello")).executor(Services.edt.executor()).add(Says.what("world")).executor(Services.t2.executor()).add(Says.what("!")).executor(Services.t1.executor()).build().invoke();
	}


	@Test
	public void building1()
		throws Exception {

		final ISayFactory say = Says.factory();
		Chains.builder().add(say.what("success")).build().invoke();
		Assert.assertEquals(say.toString(), "success");
	}


	@Test
	public void building2()
		throws Exception {

		final ISayFactory say = Says.factory();
		Chains.builder().add(say.what("succ")).add(say.what("ess")).build().invoke();
		Assert.assertEquals(say.toString(), "success");
	}


	@Test
	public void buildingOnDifferentExecutors()
		throws Exception {

		final ISayFactory say = Says.factory();
		Chains.builder()//
			.add(say.what("1"))
			.executor(Services.t1.executor())

			.add(say.what("2"))
			.executor(Services.t2.executor())

			.add(say.what("3"))
			.executor(Services.multi1.executor())

			.add(say.what("4"))
			.executor(Services.edt.executor())

			.build()
			.invoke();

		Assert.assertEquals(say.toString(), "1234");
	}
}
