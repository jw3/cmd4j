package cmd4j;

import org.testng.annotations.Test;

import cmd4j.ICommand.IObservableStateCommand;
import cmd4j.ICommand.IStateCommand;
import cmd4j.ICommand.IStateCommand.IStateCommand1;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;

/**
 * ensure that ICommand returns are always treated as executable states
 *
 * @author wassj
 *
 */
public class StateMachineTest {
	private final int expected = 5;


	@Test
	public void chains_create()
		throws Exception {

		final TestVariable<Integer> var = TestVariable.create(0);
		Chains.create(repeat(expected, var)).invoke();
		var.assertEquals(expected);
	}


	@Test
	public void chains_builder()
		throws Exception {

		final TestVariable<Integer> var = TestVariable.create(0);
		Chains.builder().add(repeat(expected, var)).build().invoke();
		var.assertEquals(expected);
	}


	@Test
	public void observable()
		throws Exception {

		final TestVariable<Integer> var = TestVariable.create(0);
		final TestVariable<Integer> otherVar = TestVariable.create(0);

		final IObservableStateCommand cmd = Observers.observable(repeat(expected, var)).after(Does.add(otherVar, 1));
		Chains.create(cmd).invoke();

		var.assertEquals(expected);
		otherVar.assertEquals(expected);
	}


	private static IStateCommand repeat(final int times, final TestVariable<Integer> var) {
		return new IStateCommand1() {
			public ICommand invoke() {
				var.set(var.get() + 1);
				return var.get() < times ? this : null;
			}
		};
	}
}
