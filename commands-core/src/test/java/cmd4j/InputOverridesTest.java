package cmd4j;

import java.io.StringWriter;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.testing.Asserts;
import cmd4j.testing.Says;

/**
 * Test a variety of different input usages
 *
 * @author wassj
 *
 */
public class InputOverridesTest {

	/**
	 * test to ensure that the input that was passed to the chain matches the command input
	 */
	@Test
	public void basicChainInput()
		throws Exception {

		final String input = "this is the input";
		Chains.builder().add(Asserts.is(input)).build().invoke(input);
	}


	@Test
	public void basicChainInputNull()
		throws Exception {

		Chains.builder().add(Asserts.is(null)).build().invoke(null);
	}


	/**
	 * test to ensure that the input that was passed to the chain matches the command input
	 */
	@Test
	public void basicLinkInput()
		throws Exception {

		final String input = "this is the input";
		Chains.builder().add(Asserts.is(input)).input(input).build().invoke();
	}


	@Test
	public void basicLinkInputNull()
		throws Exception {

		Chains.builder().add(Asserts.is(null)).input(null).build().invoke();
	}


	/**
	 * test to ensure that the input that was passed to the chain matches the command input
	 */
	@Test
	public void linkOverrideInput()
		throws Exception {

		final String chainInput = "this is the chain input";
		final String linkInput = "this is the link input";
		Chains.builder().add(Asserts.is(linkInput)).input(linkInput).add(Asserts.is(chainInput)).build().invoke(chainInput);
	}


	/**
	 * test to ensure that the input that was passed to the chain matches the command input
	 */
	@Test
	public void linkOverrideInputDifferentTypes()
		throws Exception {

		final String chainInput = "this is the chain input";
		final Integer linkInput = 1010101;
		Chains.builder().add(Asserts.is(linkInput)).input(linkInput).add(Asserts.is(chainInput)).build().invoke(chainInput);
	}


	@Test
	public void test()
		throws Exception {

		final StringWriter writer = new StringWriter();
		Chains.builder().add(Says.input(writer)).add(Says.input(writer)).input("1").add(Says.input(writer)).build().invoke("0");
		Assert.assertEquals(writer.toString(), "010");
	}
}
