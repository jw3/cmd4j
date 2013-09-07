/**
 * 
 */
package chain4j.builder;

import org.testng.annotations.Test;

import chain4j.Say;
import chain4j.Service;
import chain4j.builder.ChainBuilder;

/**
 * @author wassj
 *
 */
public class TestSimpleBuilding {

	@Test
	public void firstTest() {
		ChainBuilder.create(Say.what("hello")).executor(Service.edt.get()).add(Say.what("world")).executor(Service.b.get()).add(Say.what("!")).executor(Service.a.get()).build().run();
	}
}
