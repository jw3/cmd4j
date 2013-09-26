package cmd4j.builder;

import static cmd4j.Tests.is;

import java.io.StringWriter;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.Say;
import cmd4j.common.Chains;

/**
 * Test a variety of different dto usages
 *
 * @author wassj
 *
 */
public class TestDtoOverrides {

	/**
	 * test to ensure that the dto that was passed to the chain matches the command dto
	 */
	@Test
	public void basicChainDto()
		throws Exception {

		final String dto = "this is the dto";
		Chains.builder().add(is(dto)).build().invoke(dto);
	}


	@Test
	public void basicChainDtoNull()
		throws Exception {

		Chains.builder().add(is(null)).build().invoke(null);
	}


	/**
	 * test to ensure that the dto that was passed to the chain matches the command dto
	 */
	@Test
	public void basicLinkDto()
		throws Exception {

		final String dto = "this is the dto";
		Chains.builder().add(is(dto)).dto(dto).build().invoke();
	}


	@Test
	public void basicLinkDtoNull()
		throws Exception {

		Chains.builder().add(is(null)).dto(null).build().invoke();
	}


	/**
	 * test to ensure that the dto that was passed to the chain matches the command dto
	 */
	@Test
	public void linkOverrideDto()
		throws Exception {

		final String chainDto = "this is the chain dto";
		final String linkDto = "this is the link dto";
		Chains.builder().add(is(linkDto)).dto(linkDto).add(is(chainDto)).build().invoke(chainDto);
	}


	/**
	 * test to ensure that the dto that was passed to the chain matches the command dto
	 */
	@Test
	public void linkOverrideDtoDifferentTypes()
		throws Exception {

		final String chainDto = "this is the chain dto";
		final Integer linkDto = 1010101;
		Chains.builder().add(is(linkDto)).dto(linkDto).add(is(chainDto)).build().invoke(chainDto);
	}


	@Test
	public void test()
		throws Exception {

		final StringWriter writer = new StringWriter();
		Chains.builder().add(Say.dto(writer)).add(Say.dto(writer)).dto("1").add(Say.dto(writer)).build().invoke("0");
		Assert.assertEquals(writer.toString(), "010");
	}
}
