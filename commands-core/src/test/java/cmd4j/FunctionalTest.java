package cmd4j;

import org.testng.annotations.Test;

import cmd4j.testing.Does;
import cmd4j.testing.Does.Variable;

import com.google.common.base.Predicates;

/**
 *
 *
 * @author wassj
 *
 */
public class FunctionalTest {

	@Test
	public void test()
		throws Exception {

		{
			final boolean runs = true;
			final Variable<Boolean> var = Variable.create(false);
			Chains.create(Functional.invokeIf(Does.set(var, true), Predicates.alwaysTrue())).invoke();
			var.assertEquals(runs);
		}

		{
			final boolean runs = false;
			final Variable<Boolean> var = Variable.create(false);
			Chains.create(Functional.invokeIf(Does.set(var, true), Predicates.alwaysFalse())).invoke();
			var.assertEquals(runs);
		}
	}
}
