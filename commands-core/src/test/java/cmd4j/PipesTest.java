package cmd4j;

import mockit.Expectations;
import mockit.Mocked;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;

/**
 *
 * @author wassj
 *
 */
public class PipesTest {
	@Mocked
	private ICommand2<Integer> _2a;
	@Mocked
	private ICommand3<Integer> _3a;
	@Mocked
	private ICommand4<Integer, Integer> _4a;
	@Mocked
	private ICommand4<Integer, Integer> _4b;


	/**
	 * validate that the return value is piped into the following input command
	 */
	@Test
	public void withPipe()
		throws Exception {

		new Expectations() {
			{
				_4a.invoke(1);
				result = 2;
				_2a.invoke(2);
			}
		};

		Chains.builder()//
			.add(_4a)
			.pipe()
			.add(_2a)
			.build()
			.invoke(1);
	}


	/**
	 * validate that the input value is used rather than the previous output command if no pipe is installed
	 */
	@Test
	public void withoutPipe()
		throws Exception {

		new Expectations() {
			{
				_4a.invoke(1);
				result = 2;
				_2a.invoke(1);
			}
		};

		Chains.builder()//
			.add(_4a)
			.add(_2a)
			.build()
			.invoke(1);
	}


	/**
	 * validate that a piped value can also be used as the return value 
	 */
	@Test
	public void pipePastReturnValue()
		throws Exception {

		new Expectations() {
			{
				_4a.invoke(1);
				result = 2;
				_2a.invoke(2);
			}
		};

		final int result = Chains.builder()//
			.add(_4a)
			.pipe()
			.add(_2a)
			.returns(Integer.class)
			.build()
			.invoke(1);
		Assert.assertEquals(result, 2);
	}


	/**
	 * validate that a piped value can later be overwritten as return value 
	 */
	@Test
	public void pipeOverwrittenReturnValue()
		throws Exception {

		new Expectations() {
			{
				_4a.invoke(1);
				result = 2;
				_4b.invoke(2);
				_3a.invoke();
				result = 9;
			}
		};

		final int result = Chains.builder()//
			.add(_4a)
			.pipe()
			.add(_4b)
			.add(_3a)
			.returns(Integer.class)
			.build()
			.invoke(1);
		Assert.assertEquals(result, 9);
	}
}
