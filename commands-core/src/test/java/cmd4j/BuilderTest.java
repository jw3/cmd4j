package cmd4j;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand1;
import cmd4j.testing.Services;

/**
 * validate some various chain building uses
 * @author wassj
 *
 */
public class BuilderTest {

	@Test
	public void building1(@Mocked final ICommand1 a)
		throws Exception {

		new Expectations() {
			{
				a.invoke();
			}
		};
		Chains.builder().add(a).build().invoke();
	}


	@Test
	public void building2(@Mocked final ICommand1 a, @Mocked final ICommand1 b)
		throws Exception {

		new Expectations() {
			{
				a.invoke();
				b.invoke();
			}
		};
		Chains.builder().add(a).add(b).build().invoke();
	}


	@Test
	public void buildingOnDifferentExecutors1(@Mocked final ICommand1 a, @Mocked final ICommand1 b, @Mocked final ICommand1 c)
		throws Exception {

		new Expectations() {
			{
				a.invoke();
				b.invoke();
				c.invoke();
			}
		};

		Chains.builder() //
			.add(a)
			.executor(Services.edt.executor())
			.add(b)
			.executor(Services.t2.executor())
			.add(c)
			.executor(Services.t1.executor())
			.build()
			.invoke();
	}


	@Test
	public void buildingOnDifferentExecutors2(@Mocked final ICommand1 a, @Mocked final ICommand1 b, @Mocked final ICommand1 c, @Mocked final ICommand1 d)
		throws Exception {

		new Expectations() {
			{
				a.invoke();
				b.invoke();
				c.invoke();
				d.invoke();
			}
		};

		Chains.builder()//
			.add(a)
			.executor(Services.t1.executor())
			.add(b)
			.executor(Services.t2.executor())
			.add(c)
			.executor(Services.multi1.executor())
			.add(d)
			.executor(Services.edt.executor())
			.build()
			.invoke();
	}
}
