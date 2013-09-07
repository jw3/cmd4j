package chain4j;

import org.testng.annotations.Test;

import chain4j.builder.ChainBuilder;
import chain4j.test.Service;
import chain4j.test.Say;

/**
 * @author wassj
 *
 */
public class TestLinkSubBuilder {

	@Test
	public void test()
		throws InterruptedException {
		ChainBuilder.create(Say.what(1)).add(Say.what("...")).executor(Service.a.get()).add(Say.what(2)).build().dto("mississippi").run();
		System.out.println("done");
	}
}
