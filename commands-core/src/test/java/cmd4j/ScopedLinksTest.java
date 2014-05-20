package cmd4j;

import java.io.StringWriter;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.testing.Says;

/**
 * When a Chain is executed multiple times the link that was set on it will remain;
 * This is not a problem if the input to the link is explicitly set, but if it is to
 * be set by a parent chains input it will not be overridden.
 *
 * @author wassj
 *
 */
public class ScopedLinksTest {

	/**
	 * verify that the reusable chain will use the input from each parent chain invocation
	 * the bug is that the first invocation will be used repeatedly
	 * @throws Exception
	 */
	@Test
	public void inheritedLinkIsExecutionScoped()
		throws Exception {

		final StringWriter writer = new StringWriter();
		final IChain<Void> reusable = Chains.create(Says.input(writer));

		final IChain<Void> one = Chains.create(reusable);
		one.invoke("1");
		Assert.assertEquals(writer.toString(), "1");

		final IChain<Void> two = Chains.create(reusable);
		two.invoke("2");
		Assert.assertEquals(writer.toString(), "12");

		final IChain<Void> three = Chains.create(reusable);
		three.invoke("3");
		Assert.assertEquals(writer.toString(), "123");
	}


	@Test
	public void inheritedLinkIsExecutionScoped2()
		throws Exception {

		final StringWriter writer = new StringWriter();
		final IChain<Void> reusable = Chains.create(Says.input(writer));

		Chains.builder()//
			.add(reusable)
			.input("1")
			.add(reusable)
			.input("2")
			.add(reusable)
			.input("3")
			.build()
			.invoke("x");

		Assert.assertEquals(writer.toString(), "123");
	}


	@Test
	public void explicitSetStillOverrides()
		throws Exception {

		final StringWriter writer = new StringWriter();
		final IChain<Void> reusable = Chains.builder()//
			.add(Says.input(writer))
			.input("0")
			.build();

		final IChain<Void> one = Chains.create(reusable);
		one.invoke("1");
		Assert.assertEquals(writer.toString(), "0");

		final IChain<Void> two = Chains.create(reusable);
		two.invoke("2");
		Assert.assertEquals(writer.toString(), "00");

		final IChain<Void> three = Chains.create(reusable);
		three.invoke("3");
		Assert.assertEquals(writer.toString(), "000");
	}
}
