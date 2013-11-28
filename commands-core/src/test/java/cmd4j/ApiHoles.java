package cmd4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.testing.Does;

/**
 * a collection of API issues that will be resolved going forward and should
 * not be relied upon at any point as a valied API usage. 
 *
 * @author wassj
 *
 */
public class ApiHoles {
	/*
	 * this is a demonstration of an api detail that is in flux at the moment (11-28-2013)
	 * 
	 * Void chains actually return a value.  This is a semantic issue that will potentially cause problems
	 * in the future for clients of the API who discover and then rely upon this behavior.
	 * This issue should be addressed as soon as possible.
	 * 
	 * This behavior will be fixed in the future and should not be relied upon 
	 */
	@Test
	public void test()
		throws Exception {

		// chain that should return null as Void type
		final IChain<Void> chain = Chains.builder().add(Does.returns(100)).build();
		// instead returns the chain result 
		final Object value = chain.invoke();
		// when fixed; assert value is null should pass
		Assert.assertEquals(value, 100);
	}
}
