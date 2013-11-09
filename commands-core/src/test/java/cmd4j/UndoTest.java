package cmd4j;

import org.testng.annotations.Test;

import cmd4j.testing.Does;
import cmd4j.testing.Does.Variable;

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

		final Variable<Boolean> var = Does.var(true);

		final IChain<Void> chain = Chains.builder().add(Does.undoableSet(var, false)).build();
		chain.invoke();
		var.assertEquals(false);

		final IChain<Void> chain2 = Chains.undoable(chain);
		chain2.invoke();
		var.assertEquals(true);

		chain.invoke();
		var.assertEquals(false);
		chain2.invoke();
		var.assertEquals(true);
	}
}
