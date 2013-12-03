package cmd4j;

import java.util.concurrent.Callable;

import org.testng.annotations.Test;

import cmd4j.testing.Asserts;
import cmd4j.testing.Does.TestVariable;
import cmd4j.testing.Services;

/**
 *
 *
 * @author wassj
 *
 */
public class ConcurrentCallablesTest {

	@Test
	public void wrapCallable()
		throws Exception {

		final TestVariable<Integer> var = new TestVariable<Integer>(0);
		Chains.builder() //
			.add(Asserts.isEquals(var, 0))
			.add(Commands.from(new IncrementVariable(var)))
			.add(Asserts.isEquals(var, 1))
			.add(Commands.from(new IncrementVariable(var)))
			.add(Asserts.isEquals(var, 2))
			.build()
			.invoke();
	}


	@Test
	public void wrapLongRunning()
		throws Exception {

		final TestVariable<Integer> var = new TestVariable<Integer>(0);
		Chains.builder() //
			.add(Asserts.isEquals(var, 0))

			.add(Commands.from(Services.t1.executor().submit(new IncrementVariable(var, 100))))
			.add(Asserts.isEquals(var, 1))

			.add(Commands.from(Services.t2.executor().submit(new IncrementVariable(var, 200))))
			.add(Asserts.isEquals(var, 2))

			.add(Commands.from(Services.t2.executor().submit(new IncrementVariable(var, 300))))
			.add(Asserts.isEquals(var, 3))

			.build()
			.invoke();
	}


	static class IncrementVariable
		implements Callable<Void> {

		private final TestVariable<Integer> var;
		private final long delay;


		public IncrementVariable(final TestVariable<Integer> var) {
			this(var, 0);
		}


		public IncrementVariable(final TestVariable<Integer> var, final long delay) {
			this.var = var;
			this.delay = delay;
		}


		public Void call()
			throws Exception {

			Thread.sleep(delay);
			var.set(var.get() + 1);

			return null;
		}
	}
}
