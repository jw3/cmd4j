package cmd4j;

import java.util.UUID;

import junit.framework.Assert;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2;
import cmd4j.testing.Does;
import cmd4j.testing.Does.Boom;
import cmd4j.testing.Does.TestVariable;

/**
 * test a short circuiting chain which only will run one of its commands
 *
 * @author wassj
 *
 */
public class RunOneTest {

	/*
	 * var is set
	 */
	@Test
	public void test0()
		throws Exception {

		final String val = UUID.randomUUID().toString().substring(0, 7);
		final TestVariable<String> var = TestVariable.create();
		Chains.builder().add(Commands.onlyOne(Does.set(var))).build().invoke(val);
		var.assertEquals(val);
	}


	/*
	 * nothing runs, var is not set
	 */
	@Test
	public void test1()
		throws Exception {

		final String val = UUID.randomUUID().toString().substring(0, 7);
		final TestVariable<String> var = TestVariable.create();
		Chains.builder().add(Commands.onlyOne(Does.nothing(), Does.set(var))).build().invoke(val);
		var.assertEquals(null);
	}


	/*
	 * var is set, boom is skipped
	 */
	@Test
	public void test2()
		throws Exception {

		final String val = UUID.randomUUID().toString().substring(0, 7);
		final TestVariable<String> var = TestVariable.create();
		Chains.builder().add(Commands.onlyOne(Does.set(var), Does.boom())).build().invoke(val);
		var.assertEquals(val);
	}


	/*
	 * boom
	 */
	@Test(expectedExceptions = Boom.class)
	public void test3()
		throws Exception {

		Chains.builder().add(Commands.onlyOne(Does.boom())).build().invoke();
	}


	/*
	 * nothing runs, boom is skipped
	 */
	@Test
	public void test4()
		throws Exception {

		Chains.builder().add(Commands.onlyOne(Does.nothing(), Does.boom())).build().invoke();
	}


	// returns

	@Test
	public void returns1()
		throws Exception {

		final String expected = UUID.randomUUID().toString().substring(0, 7);
		final String actual = Chains.builder().add(Commands.onlyOne(Does.returns(expected), Does.boom())).returns(String.class).build().invoke();
		Assert.assertEquals(expected, actual);
	}


	/*
	 * fix bug where the ran flag was being set when in visit mode but on unvisited nodes
	 * the bug caused the following to short circuit before the second command was run
	 */
	@Test
	public void visitBug()
		throws Exception {

		final TestVariable<String> strvar = TestVariable.create("string");
		Chains.builder().add(Commands.onlyOne(new ICommand2<Integer>() {
			public void invoke(final Integer input)
				throws Exception {
				throw new Exception("shouldnt have run");
			}
		}, Does.set(strvar), new ICommand2<Integer>() {
			public void invoke(final Integer input)
				throws Exception {
				throw new Exception("shouldnt have run");
			}
		})).build().invoke("fooooooooo");
		strvar.assertEquals("fooooooooo");
	}
}
