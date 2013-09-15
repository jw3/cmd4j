package chain4j.builder;

import org.testng.annotations.Test;

import chain4j.IChain;
import chain4j.Say;
import chain4j.Service;

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

		final IChain chain1 = ChainBuilder.create(Say.what(1)).add(Say.what("...")).executor(Service.t1.executor()).add(Say.what(2)).build();
		final IChain chain2 = ChainBuilder.create(Say.what(3)).add(Say.what("...")).executor(Service.t1.executor()).add(Say.what(4)).build();

		ChainBuilder.create()//
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
