package cmd4j.common;

import cmd4j.Chains;
import cmd4j.Commands;
import cmd4j.common.WrapCallableTest.IncrementVariable;
import cmd4j.testing.AssertCommands;
import cmd4j.testing.Service;
import cmd4j.testing.Tests.Variable;

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
}
