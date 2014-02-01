package cmd4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.IFunction;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.ICommand.IStateCommand;
import cmd4j.ICommand.IStateCommand.IStateCommand3;
import cmd4j.ICommand.IStateCommand.IStateCommand4;
import cmd4j.testing.CmdTests;
import cmd4j.testing.Does;

import com.google.common.base.Preconditions;

/**
 * validate the behavior of {@link IStateCommand3} and {@link IStateCommand4}
 * @author wassj
 *
 */
public class ReturningStateCommandsTest {

	@Test
	// validate the returning command returning null
	public void nulls1()
		throws Exception {

		final Object actual = Chains.builder().add(returns(null)).returns().build().invoke();
		Assert.assertNull(actual);
	}


	@Test
	// validate the state command returning null
	public void nulls2()
		throws Exception {

		final Object actual = Chains.builder().add(noState()).returns().build().invoke();
		Assert.assertNull(actual);
	}


	@Test
	// validate the state command returning a value which is piped to another command
	public void piped()
		throws Exception {

		final String expected = CmdTests.random(5);
		final Object actual = Chains.builder().add(returns(expected)).pipe().add(toUpper()).returns().build().invoke();
		Assert.assertEquals(actual, expected.toUpperCase());
	}


	@Test
	// validate a chainbuilder returing a value from a IStateCommand3
	public void chainBuilder3()
		throws Exception {

		final String expected = CmdTests.random(5);
		final Object actual = Chains.builder().add(returns(expected)).returns().build().invoke();
		Assert.assertEquals(actual, expected);
	}


	@Test
	// validate a chainbuilder returing a value from a IStateCommand3
	public void chainBuilder4()
		throws Exception {

		final String expected = CmdTests.random(5);
		final Object actual = Chains.builder().add(returns()).returns().build().invoke(expected);
		Assert.assertEquals(actual, expected);
	}


	@Test
	// validate a state command that returns a state command which returns a value
	public void stepped()
		throws Exception {

		final String expected = CmdTests.random(5);
		final Object actual = Chains.builder().add(returns2(expected)).returns().build().invoke();
		Assert.assertEquals(actual, expected);
	}


	//

	/**
	 * function; toUpper a string
	 * @return
	 */
	private static IFunction<String, String> toUpper() {
		return new IFunction<String, String>() {
			public String invoke(final String input) {
				return Preconditions.checkNotNull(input).toUpperCase();
			}
		};
	}


	/**
	 * create a {@link IStateCommand4} which returns the input value
	 * @return
	 */
	private static <O> IReturningCommand<O> returns() {
		return new IStateCommand4<O, O>() {
			public IReturningCommand<O> invoke(final O input) {
				return Does.returns(input);
			}
		};
	}


	/**
	 * create a {@link IStateCommand3} which returns the specified value
	 * @param value
	 * @return
	 */
	private static <O> IReturningCommand<O> returns(final O value) {
		return new IStateCommand3<O>() {
			public IReturningCommand<O> invoke() {
				return Does.returns(value);
			}
		};
	}


	/**
	 * create a {@link IStateCommand3} which returns a {@link IStateCommand} as the {@link IReturningCommand}
	 * @param value
	 * @return
	 */
	private static <O> IReturningCommand<O> returns2(final O value) {
		return new IStateCommand3<O>() {
			public IReturningCommand<O> invoke() {
				return returns(value);
			}
		};
	}


	/**
	 * create a {@link IStateCommand3} which returns a {@link IStateCommand} as the {@link IReturningCommand}
	 * @param value
	 * @return
	 */
	private static <O> IReturningCommand<O> noState() {
		return new IStateCommand3<O>() {
			public IReturningCommand<O> invoke() {
				return null;
			}
		};
	}
}
