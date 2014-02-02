package cmd4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.IChain.IUndoChain;
import cmd4j.ICommand.IStateCommand.IStateCommand1;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;

/**
 * --under-tested--
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


	@Test
	public void stateCommandWrapper()
		throws Exception {

		{
			final TestVariable<Boolean> var = Does.var(true);
			final IChain<Void> chain = Chains.builder().add(Does.undoableSet(var, false)).build();
			test(wrapInStateCommand(chain), var);
		}
		{
			final TestVariable<Boolean> var = Does.var(true);
			final IChain<Void> chain = Chains.builder().add(Does.undoableSet2(var, false)).build();
			test(wrapInStateCommand(chain), var);
		}
	}


	private static ICommand wrapInStateCommand(final ICommand command) {
		return new IStateCommand1.IUndo() {
			public ICommand invoke() {
				return command;
			}


			public ICommand undo() {
				return Chains.undoable(Chains.create(command));
			}
		};
	}


	private static void test(final ICommand command, final TestVariable<Boolean> var)
		throws Exception {

		final IChain<Void> chain = Chains.create(command);
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
