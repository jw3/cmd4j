package cmd4j;

import org.testng.annotations.Test;

import cmd4j.IChain.IUndoChain;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;

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

		final TestVariable<Boolean> var = Does.var(true);

		final IChain<Void> chain = Chains.builder().add(Does.undoableSet(var, false)).build();
		chain.invoke();
		var.assertEquals(false);

		final IUndoChain<Void> undo = Chains.undoable(chain);
		undo.undo();
		var.assertEquals(true);

		chain.invoke();
		var.assertEquals(false);
		undo.undo();
		var.assertEquals(true);
	}
}
