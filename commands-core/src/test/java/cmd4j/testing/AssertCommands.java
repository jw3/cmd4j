package cmd4j.testing;

import org.testng.Assert;

import cmd4j.ICommand;
import cmd4j.ICommand.ICommand1;
import cmd4j.testing.Tests.Variable;

/**
 *
 *
 * @author wassj
 *
 */
public enum AssertCommands {
	/*noinstance*/;

	public static <T> ICommand assertEquals(final Variable<T> var, final T val) {
		return new ICommand1() {
			public void invoke() {
				Assert.assertEquals(val, var.getValue());
			}
		};
	}
}
