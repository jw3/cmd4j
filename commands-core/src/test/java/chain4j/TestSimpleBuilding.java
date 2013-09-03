/**
 * 
 */
package chain4j;

import org.testng.annotations.Test;

import chain4j.builder.ChainBuilder;
import chain4j.test.Exec;
import chain4j.test.Say;

/**
 * @author wassj
 *
 */
public class TestSimpleBuilding {

	@Test
	public void firstTest() {
		ChainBuilder.create(Say.what("hello")).executor(Exec.edt.get()).add(Say.what("world")).executor(Exec.b.get()).add(Say.what("!")).executor(Exec.a.get()).build().exec();
	}
}
