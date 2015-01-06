package cmd4j;

import java.util.Collection;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand3;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

/**
 *
 * @author wassj
 *
 */
public class RunUntilTest {

	/**
	 * first one satifies the condition, second one doesnt run
	 */
	@Test
	public void firstCommandSatisfies(@Mocked final ICommand3<Integer> c1, @Mocked final ICommand3<Integer> c2)
		throws Exception {

		new Expectations() {
			{
				c1.invoke();
				result = 1;
			}
		};

		final ICommand until = Commands.until(list(c1, c2), Predicates.equalTo(1));
		Chains.create(until).invoke();
	}


	@Test
	public void secondCommandSatisfies(@Mocked final ICommand3<Integer> c1, @Mocked final ICommand3<Integer> c2)
		throws Exception {

		new Expectations() {
			{
				c1.invoke();
				result = 1;
				c2.invoke();
				result = 2;
			}
		};

		final ICommand until = Commands.until(list(c1, c2), Predicates.equalTo(2));
		Chains.create(until).invoke();
	}


	@Test
	public void mixedTypeSecondCommandSatisfies(@Mocked final ICommand3<String> c1, @Mocked final ICommand3<Integer> c2)
		throws Exception {

		new Expectations() {
			{
				c1.invoke();
				result = "foo";
				c2.invoke();
				result = 1;
			}
		};

		final ICommand until = Commands.until(list(c1, c2), Predicates.equalTo(1));
		Chains.create(until).invoke();
	}


	@Test
	public void mixedNeitherSatisfies(@Mocked final ICommand3<String> c1, @Mocked final ICommand3<Integer> c2)
		throws Exception {

		new Expectations() {
			{
				c1.invoke();
				result = "foo";
				c2.invoke();
				result = 1;
			}
		};

		final ICommand until = Commands.until(list(c1, c2), Predicates.equalTo(2));
		Chains.create(until).invoke();
	}


	/**
	 * wont match
	 */
	@Test
	public void noMatch(@Mocked final ICommand3<Integer> c1, @Mocked final ICommand3<Integer> c2)
		throws Exception {

		new Expectations() {
			{
				c1.invoke();
				result = 1;
				c2.invoke();
				result = 2;
			}
		};

		final ICommand until = Commands.until(list(c1, c2), Predicates.equalTo(10));
		Chains.create(until).invoke();
	}


	/**
	 * wont match / wrong type
	 */
	@Test
	public void noMatch2(@Mocked final ICommand3<Integer> c1, @Mocked final ICommand3<Integer> c2)
		throws Exception {

		new Expectations() {
			{
				c1.invoke();
				result = 1;
				c2.invoke();
				result = 2;
			}
		};

		final ICommand until = Commands.until(list(c1, c2), Predicates.equalTo("different"));
		Chains.create(until).invoke();
	}


	private static Collection<ICommand> list(final ICommand... commands) {
		return Lists.newArrayList(commands);
	}
}
