/**
 * 
 */
package chain4j;

import org.testng.annotations.Test;

import chain4j.test.Exec;
import chain4j.test.Say;

/**
 * @author wassj
 *
 */
public class TestSimpleBuilding {

	@Test
	public void firstTest() {
		ChainBuilder.create(Say.now("hello")).executor(Exec.edt.get()).add(Say.now("world")).executor(Exec.b.get()).add(Say.now("!")).executor(Exec.c.get()).build().exec();
	}
}
