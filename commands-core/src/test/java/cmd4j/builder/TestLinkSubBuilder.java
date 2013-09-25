package cmd4j.builder;

import org.testng.annotations.Test;

import cmd4j.Say;
import cmd4j.Service;
import cmd4j.common.Chains;

/**
 * @author wassj
 *
 */
public class TestLinkSubBuilder {

	@Test
	public void test()
		throws Exception {
		Chains.builder().add(Say.what(1)).add(Say.what("...")).executor(Service.t1.executor()).add(Say.what(2)).build().invoke("mississippi");
		System.out.println("done");
	}
}
