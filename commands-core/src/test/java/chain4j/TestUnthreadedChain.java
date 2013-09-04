package chain4j;

import org.testng.annotations.Test;

import chain4j.builder.ChainBuilder;
import chain4j.test.Service;
import chain4j.test.Say;

/**
 *
 *
 * @author wassj
 *
 */
public class TestUnthreadedChain {

	@Test
	public void test() {
		ChainBuilder.create(Say.what(1)).add(Say.what("...")).executor(Service.a.get()).add(Say.what(2)).unthreaded(true).build().dto("mississippi").exec();
	}
}
