package chain4j.builder;

import org.testng.annotations.Test;

import chain4j.Say;
import chain4j.Service;
import chain4j.builder.ChainBuilder;

/**
 *
 *
 * @author wassj
 *
 */
public class TestUnthreadedChain {

	@Test
	public void test() {
		ChainBuilder.create(Say.what(1))//
			.add(Say.what("..."))
			//
			.executor(Service.a.get())
			//
			.add(Say.what(2))
			//
			.buildUnthreaded()
			//
			.dto("mississippi")
			//
			.run();
	}
}
