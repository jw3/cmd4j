package cmd4j;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand3;
import cmd4j.testing.Does.Variable;

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

		final Variable<Integer> var = Variable.create(0);
		Chains.create(repeat(expected, var)).invoke();
		var.assertEquals(expected);
	}


	@Test
	public void chains_builder()
		throws Exception {

		final Variable<Integer> var = Variable.create(0);
		Chains.builder().add(repeat(expected, var)).build().invoke();
		var.assertEquals(expected);
	}


	@Test
	public void commands_invoke()
		throws Exception {

		final Variable<Integer> var = Variable.create(0);
		Commands.invoke(repeat(expected, var));
		var.assertEquals(expected);
	}


	private static ICommand repeat(final int times, final Variable<Integer> var) {
		return new ICommand3<ICommand>() {
			public ICommand invoke() {
				var.setValue(var.getValue() + 1);
				return var.getValue() < times ? this : null;
			}
		};
	}
}
