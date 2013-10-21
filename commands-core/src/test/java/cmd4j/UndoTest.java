package cmd4j;

import org.testng.annotations.Test;

import cmd4j.testing.Tests;
import cmd4j.testing.Tests.Variable;

/**
 *
 *
 * @author wassj
 *
 */
public class UndoTest {

	@Test
	public void test()
		throws Exception {

		final Variable<Boolean> var = Tests.var(true);

		final IChain chain = Chains.builder().add(Tests.undoableSet(var, false)).build();
		chain.invoke();
		var.assertEquals(false);

		final IChain chain2 = Chains.makeUndoable(chain);
		chain2.invoke();
		var.assertEquals(true);

		chain.invoke();
		var.assertEquals(false);
		chain2.invoke();
		var.assertEquals(true);
	}
}
