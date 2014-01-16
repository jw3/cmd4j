package cmd4j;

import org.testng.Assert;
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

		{
			final TestVariable<Boolean> var = Does.var(true);
			final IChain<Void> chain = Chains.builder().add(Does.undoableSet(var, false)).build();
			test(chain, var);
		}
		{
			final TestVariable<Boolean> var = Does.var(true);
			final IChain<Void> chain = Chains.builder().add(Does.undoableSet2(var, false)).build();
			test(chain, var);
		}
	}


	public void test(final IChain<Void> chain, final TestVariable<Boolean> var)
		throws Exception {

		chain.invoke();
		var.assertEquals(false);

		final IUndoChain<Void> undo = Chains.undoable(chain);

		// ensure that the rewrapped chain is the same object
		Assert.assertSame(undo, Chains.undoable(undo));

		undo.undo();
		var.assertEquals(true);

		chain.invoke();
		var.assertEquals(false);
		undo.undo();
		var.assertEquals(true);
	}
}
