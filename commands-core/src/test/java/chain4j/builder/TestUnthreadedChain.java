package chain4j.builder;

import org.testng.annotations.Test;

import chain4j.Say;
import chain4j.Service;
import chain4j.common.ChainBuilder;

/**
 *
 *
 * @author wassj
 *
 */
public class TestUnthreadedChain {

	@Test
	public void test()
		throws Exception {

		ChainBuilder.create(Say.what(1))//
			.add(Say.what("..."))
			//
			.executor(Service.t1.executor())
			//
			.add(Say.what(2))
			//
			.buildUnthreaded()
			//
			.invoke("mississippi");
	}
}
