package chain4j.builder;

import org.testng.annotations.Test;

import chain4j.Say;

/**
 * Test the function of specifying a DTO for a link while the chain has a different DTO specified.
 *
 * @author wassj
 *
 */
public class TestDtoOverrides {

	@Test
	public void test()
		throws Exception {

		ChainBuilder.create(Say.dto()).add(Say.dto()).dto("override").add(Say.dto()).build().invoke("chain");
	}
}
