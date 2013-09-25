package cmd4j.builder;

import org.testng.annotations.Test;

import cmd4j.IChain;
import cmd4j.Say;
import cmd4j.Service;
import cmd4j.common.Chains;

/**
 *
 *
 * @author wassj
 *
 */
public class ChainedChainsTest {

	@Test
	public void test()
		throws Exception {

		final IChain chain1 = Chains.builder().add(Say.what(1)).add(Say.what("...")).executor(Service.t1.executor()).add(Say.what(2)).build();
		final IChain chain2 = Chains.builder().add(Say.what(3)).add(Say.what("...")).executor(Service.t1.executor()).add(Say.what(4)).build();

		Chains.builder()//
			.add(chain1)
			//
			.add(chain2)
			//
			.build()
			//
			.invoke("mississippi");

		System.out.println("done");
	}
}
