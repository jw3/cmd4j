package chain4j;

import org.testng.annotations.Test;

import chain4j.builder.ChainBuilder;
import chain4j.test.Say;
import chain4j.test.Service;

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
			.exec();
	}
}
