package cmd4j;

import java.io.InputStream;

import javax.annotation.Nullable;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IInputCommand;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.testing.Does.TestVariable;

/**
* validate the type inspection logic; particularly when there are complex generic extensions of command and chain
*
* @author wassj
*
*/

public class InputTypesafety2Test {

	/*
	* test commands are designed to represent a situation where a top level command
	* defines a return type, and there is a constant input type that all instances get
	* 
	* super<o> returns o, but is unusable otherwise (not a icommandX impl)
	* sub<o> is a super<o> and has a constant return type (icommand4) of Integer
	* 
	* stringReturningSub() will create an instance of sub that returns a InputStream
	*/
	public interface ISuper<O>
		extends IReturningCommand<O> {
	}


	public interface ISub<O>
		extends ISuper<O>, IInputCommand<String>, ICommand4<String, O> {
	}


	public static ISub<InputStream> inputStreamReturningSub() {
		return inputStreamReturningSub(null);
	}


	public static ISub<InputStream> inputStreamReturningSub(@Nullable final TestVariable<Boolean> ran) {
		return new ISub<InputStream>() {
			public InputStream invoke(final String input) {
				if (ran != null) {
					ran.set(true);
				}
				return null;
			}
		};
	}


	/* there is potential for error depending on the type of input, whether it
	* a- matches any wrong generic type on the command, 
	* b- matches the correct index of generic type on the command
	* c- matches no generic type on the command
	* 
	* test all three situations here
	*/

	// input does not fit expect an exception
	@Test(expectedExceptions = Exception.class)
	public void failure_setVisitImplicit()
		throws Exception {

		Chains.builder().add(inputStreamReturningSub()).build().invoke(1);
	}


	@Test
	public void success_setVisitImplicit()
		throws Exception {

		final TestVariable<Boolean> ran = TestVariable.create();
		Chains.builder().add(inputStreamReturningSub(ran)).build().invoke("");
		ran.assertEquals(true);
	}


	@Test
	public void failure_setVisitExplicit()
		throws Exception {

		Chains.builder().add(inputStreamReturningSub()).visits(true).build().invoke(1);
	}


	@Test
	public void success_setVisitExplicit()
		throws Exception {

		final TestVariable<Boolean> ran = TestVariable.create();
		Chains.builder().add(inputStreamReturningSub(ran)).visits(true).build().invoke("");
		ran.assertEquals(true);
	}


	@Test(expectedExceptions = Exception.class)
	public void failure_setNotVisitExplicit()
		throws Exception {

		Chains.builder().add(inputStreamReturningSub()).visits(false).build().invoke(1);
	}


	@Test
	public void success_setNotVisitExplicit()
		throws Exception {

		final TestVariable<Boolean> ran = TestVariable.create();
		Chains.builder().add(inputStreamReturningSub(ran)).visits(false).build().invoke("");
		ran.assertEquals(true);
	}
}
