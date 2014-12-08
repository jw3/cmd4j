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
	private ICommand2<Integer> _2b;
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


	/**
	 * Illustrates that a pipe will overwrite the input value for the remainder of the execution scope, not just for the next command
	 * 
	 * test a chain that 
	 * 1) receives an input value
	 * 2) executes a command with that value
	 * 3) #2 command pipes out its result
	 * 4) #3 command receives #2 output
	 * 5) #4 command revceive #2 output
	 * 
	 */
	@Test
	public void pipeOverwritesInput()
		throws Exception {

		final Integer original = 1;
		final Integer replaced = 2;

		new Expectations() {
			{
				_4a.invoke(original);
				times = 1;
				result = replaced;
				_2a.invoke(replaced);
				times = 1;
				_2b.invoke(replaced);
				times = 1;
			}
		};

		Chains.builder() //
			.add(_4a)
			.add(Commands.pipe())
			.add(_2a)
			.add(_2b)
			.build()
			.invoke(original);
	}
}
