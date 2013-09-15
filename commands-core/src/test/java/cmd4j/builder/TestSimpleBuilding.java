/**
 * 
 */
package cmd4j.builder;

import org.testng.annotations.Test;

import cmd4j.Say;
import cmd4j.Service;
import cmd4j.common.ChainBuilder;

/**
 * @author wassj
 *
 */
public class TestSimpleBuilding {

	@Test
	public void firstTest()
		throws Exception {

		ChainBuilder.create(Say.what("hello")).executor(Service.edt.executor()).add(Say.what("world")).executor(Service.t2.executor()).add(Say.what("!")).executor(Service.t1.executor()).build().invoke();
	}
}
