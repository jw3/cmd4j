package cmd4j;

import java.io.InputStream;

import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IDtoCommand;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.testing.Does.Variable;

/**
* validate the type inspection logic; particularly when there are complex generic extensions of command and chain
*
* @author wassj
*
*/

public class DtoTypesafetyTest2 {

	/*
	* test commands are designed to represent a situation where a top level command
	* defines a return type, and there is a constant dto type that all instances get
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
		extends ISuper<O>, IDtoCommand<String>, ICommand4<String, O> {
	}


	public static ISub<InputStream> inputStreamReturningSub() {
		return inputStreamReturningSub(null);
	}


	public static ISub<InputStream> inputStreamReturningSub(final Variable<Boolean> ran) {
		return new ISub<InputStream>() {
			public InputStream invoke(final String dto) {
				if (ran != null) {
					ran.setValue(true);
				}
				return null;
			}
		};
	}


	/* there is potential for error depending on the type of dto, whether it
	* a- matches any wrong generic type on the command, 
	* b- matches the correct index of generic type on the command
	* c- matches no generic type on the command
	* 
	* test all three situations here
	*/

	// dto does not fit expect an exception
	//@Test(expectedExceptions = ExecutionException.class, dataProvider = "failure")
	public void failure_setVisitImplicit(final Object dto)
		throws Exception {

		Chains.builder().add(inputStreamReturningSub()).build().invoke(dto);
	}


	//@Test(dataProvider = "success")
	public void success_setVisitImplicit(final Object dto)
		throws Exception {

		final Variable<Boolean> ran = Variable.create();
		Chains.builder().add(inputStreamReturningSub(ran)).build().invoke(dto);
		ran.assertEquals(true);
	}


	//@Test(expectedExceptions = ExecutionException.class, dataProvider = "failure")
	public void failure_setVisitExplicit(final Object dto)
		throws Exception {

		Chains.builder().add(inputStreamReturningSub()).visits(true).build().invoke(dto);
	}


	//@Test(dataProvider = "success")
	public void success_setVisitExplicit(final Object dto)
		throws Exception {

		final Variable<Boolean> ran = Variable.create();
		Chains.builder().add(inputStreamReturningSub(ran)).visits(true).build().invoke(dto);
		ran.assertEquals(true);
	}


	//@Test(expectedExceptions = ExecutionException.class, dataProvider = "failure")
	public void failure_setNotVisitExplicit(final Object dto)
		throws Exception {

		Chains.builder().add(inputStreamReturningSub()).visits(false).build().invoke(dto);
	}


	//@Test(dataProvider = "success")
	public void success_setNotVisitExplicit(final Object dto)
		throws Exception {

		final Variable<Boolean> ran = Variable.create();
		Chains.builder().add(inputStreamReturningSub(ran)).visits(false).build().invoke(dto);
		ran.assertEquals(true);
	}
}
