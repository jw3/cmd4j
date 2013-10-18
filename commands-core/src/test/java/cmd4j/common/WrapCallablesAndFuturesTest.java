package cmd4j.common;

import java.util.concurrent.Callable;

import org.testng.annotations.Test;

import cmd4j.testing.AssertCommands;
import cmd4j.testing.Service;
import cmd4j.testing.Tests.Variable;

/**
 *
 *
 * @author wassj
 *
 */
public class WrapCallablesAndFuturesTest {

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


	@Test
	public void wrapFuture()
		throws Exception {

		final Variable<Integer> var = new Variable<Integer>(0);
		Chains.builder() //
			.add(AssertCommands.assertEquals(var, 0))
			.add(Commands.future(Service.t1.executor().submit(new IncrementVariable(var, 1000))))
			.add(AssertCommands.assertEquals(var, 1))
			.add(Commands.future(Service.t2.executor().submit(new IncrementVariable(var, 1000))))
			.add(AssertCommands.assertEquals(var, 2))
			.add(Commands.future(Service.t2.executor().submit(new IncrementVariable(var, 1000))))
			.add(AssertCommands.assertEquals(var, 3))
			.build()
			.invoke();
	}


	private static class IncrementVariable
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

			Thread.currentThread().sleep(delay);
			var.setValue(var.getValue() + 1);

			return null;
		}
	}
}
