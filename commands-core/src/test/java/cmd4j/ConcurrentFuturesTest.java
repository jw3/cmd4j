package cmd4j;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand1;
import cmd4j.testing.Asserts;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;
import cmd4j.testing.Services;

/**
 *
 *
 * @author wassj
 *
 */
public class ConcurrentFuturesTest {

	// timing is off on this, will have to thing through the approach for testing wrapped futures
	@Test
	public void submitFuture()
		throws Exception {

		final TestVariable<Integer> var = new TestVariable<Integer>(0);
		Chains.builder() //
			.add(Asserts.isEquals(var, 0))

			.add(Does.submits(new IncrementVariableCommand(var), Services.t1.executor()))
			.add(Asserts.isEquals(var, 1))
			.add(Commands.waitFor(1))

			.add(Does.submits(new IncrementVariableCommand(var), Services.t2.executor()))
			.add(Asserts.isEquals(var, 2))
			.add(Commands.waitFor(1))

			.add(Does.submits(new IncrementVariableCommand(var), Services.multi1.executor()))
			.add(Asserts.isEquals(var, 3))
			.add(Commands.waitFor(1))

			.build()
			.invoke();
	}


	static class IncrementVariableCommand
		implements ICommand1 {

		private final TestVariable<Integer> var;


		public IncrementVariableCommand(final TestVariable<Integer> var) {
			this.var = var;
		}


		public void invoke()
			throws Exception {

			var.set(var.get() + 1);
		}
	}
}
