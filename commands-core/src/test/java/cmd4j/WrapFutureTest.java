package cmd4j;

import cmd4j.WrapCallableTest.IncrementVariable;
import cmd4j.testing.Asserts;
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
	//	@Test
	public void wrapFuture()
		throws Exception {

		final Variable<Integer> var = new Variable<Integer>(0);
		Chains.builder() //
			.add(Asserts.isEquals(var, 0))
			.add(Commands.future(Services.t1.executor().submit(new IncrementVariable(var, 1000))))
			.add(Asserts.isEquals(var, 1))
			.add(Commands.future(Services.t2.executor().submit(new IncrementVariable(var, 1000))))
			.add(Asserts.isEquals(var, 2))
			.add(Commands.future(Services.t2.executor().submit(new IncrementVariable(var, 1000))))
			.add(Asserts.isEquals(var, 3))
			.build()
			.invoke();
	}
}
