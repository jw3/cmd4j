package cmd4j;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand1;
import cmd4j.testing.Asserts;
import cmd4j.testing.Does;
import cmd4j.testing.Does.Variable;
import cmd4j.testing.Services;

/**
 *
 *
 * @author wassj
 *
 */
public class WrapFutureTest {

	// timing is off on this, will have to thing through the approach for testing wrapped futures
	@Test
	public void submitFuture()
		throws Exception {

		final Variable<Integer> var = new Variable<Integer>(0);
		Chains.builder() //
			.add(Asserts.isEquals(var, 0))

			.add(Does.submits(new IncrementVariableCommand(var), Services.t1.executor()))
			.add(Asserts.isEquals(var, 1))
			.add(Concurrent.waitFor(1))

			.add(Does.submits(new IncrementVariableCommand(var), Services.t2.executor()))
			.add(Asserts.isEquals(var, 2))
			.add(Concurrent.waitFor(1))

			.add(Does.submits(new IncrementVariableCommand(var), Services.multi1.executor()))
			.add(Asserts.isEquals(var, 3))
			.add(Concurrent.waitFor(1))

			.build()
			.invoke();
	}


	static class IncrementVariableCommand
		implements ICommand1 {

		private final Variable<Integer> var;


		public IncrementVariableCommand(final Variable<Integer> var) {
			this.var = var;
		}


		public void invoke()
			throws Exception {

			var.setValue(var.getValue() + 1);
		}
	}
}
