package cmd4j;

import java.util.concurrent.Callable;

import org.testng.annotations.Test;

import cmd4j.Chains;
import cmd4j.Commands;
import cmd4j.testing.AssertCommands;
import cmd4j.testing.Tests.Variable;

/**
 *
 *
 * @author wassj
 *
 */
public class WrapCallableTest {

	@Test
	public void wrapCallable()
		throws Exception {

		final Variable<Integer> var = new Variable<Integer>(0);
		Chains.builder() //
			.add(AssertCommands.assertEquals(var, 0))
			.add(Commands.callable(new IncrementVariable(var)))
			.add(AssertCommands.assertEquals(var, 1))
			.add(Commands.callable(new IncrementVariable(var)))
			.add(AssertCommands.assertEquals(var, 2))
			.build()
			.invoke();
	}


	static class IncrementVariable
		implements Callable<Void> {

		private final Variable<Integer> var;
		private final long delay;


		public IncrementVariable(final Variable<Integer> var) {
			this(var, 0);
		}


		public IncrementVariable(final Variable<Integer> var, final long delay) {
			this.var = var;
			this.delay = delay;
		}


		public Void call()
			throws Exception {

			Thread.sleep(delay);
			var.setValue(var.getValue() + 1);

			return null;
		}
	}
}