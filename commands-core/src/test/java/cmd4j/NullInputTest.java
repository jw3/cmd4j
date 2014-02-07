package cmd4j;

import javax.annotation.Nullable;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.IStateCommand;
import cmd4j.ICommand.IStateCommand.IStateCommand1;
import cmd4j.ICommand.IStateCommand.IStateCommand2;
import cmd4j.testing.Does;
import cmd4j.testing.Does.TestVariable;

/**
 * validate behavior with null input value
 * @author wassj
 *
 */
public class NullInputTest {

	@Test
	public void test()
		throws Exception {

		final TestVariable<String> var1 = TestVariable.create("foo");
		final TestVariable<String> var2 = TestVariable.create("foo");

		Chains.create(Does.set(var1), Does.set(var2, null)).invoke();

		Assert.assertTrue(var1.isNull());
		Assert.assertTrue(var2.isNull());
	}


	@Test
	public void stateCommand()
		throws Exception {

		final TestVariable<String> var1 = TestVariable.create("foo");
		final TestVariable<String> var2 = TestVariable.create("foo");

		Chains.create(set(var1), set(var2, null)).invoke();

		Assert.assertTrue(var1.isNull());
		Assert.assertTrue(var2.isNull());
	}


	public static <T> IStateCommand set(final TestVariable<T> v, @Nullable final T value) {
		return new IStateCommand1() {
			public ICommand invoke() {
				v.set(value);
				return null;
			}
		};
	}


	public static <T> IStateCommand set(final TestVariable<T> v) {
		return new IStateCommand2<T>() {
			public ICommand invoke(final T value) {
				v.set(value);
				return null;
			}
		};
	}
}
