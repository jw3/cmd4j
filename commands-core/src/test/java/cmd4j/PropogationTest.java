package cmd4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.testing.Does;
import cmd4j.testing.Does.Boom;
import cmd4j.testing.Does.RuntimeBoom;

/**
 *
 * @author wassj
 *
 */
public class PropogationTest {
	private final ExecutorService executor = Executors.newSingleThreadExecutor();


	@Test(expectedExceptions = RuntimeBoom.class)
	public void submitted_rtex()
		throws Throwable {

		try {
			Commands.submit(Chains.create(Does.runtimeBoom()), executor).get();
		}
		catch (final ExecutionException e) {
			throw e.getCause();
		}
		catch (final Exception e) {
			Assert.fail();
		}
	}


	@Test(expectedExceptions = Boom.class)
	public void submitted_ex()
		throws Throwable {

		try {
			Commands.submit(Chains.create(Does.boom()), executor).get();
		}
		catch (final ExecutionException e) {
			throw e.getCause();
		}
		catch (final Exception e) {
			Assert.fail();
		}
	}

	//	private static final Function<Exception, Exception> executionExceptionTransform = new Function<Exception, Exception>() {
	//		public Exception apply(final Exception input) {
	//			if (input instanceof ExecutionException) {
	//				final Throwable cause = input.getCause();
	//				return cause instanceof Exception ? (Exception)cause : new Exception(cause.getMessage(), cause);
	//			}
	//			return input;
	//		}
	//	};
}
