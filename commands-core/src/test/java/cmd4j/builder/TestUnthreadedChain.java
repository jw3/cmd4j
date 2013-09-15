package cmd4j.builder;

import org.testng.annotations.Test;

import cmd4j.Say;
import cmd4j.Service;
import cmd4j.common.ChainBuilder;

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
