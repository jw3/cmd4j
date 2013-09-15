package chain4j.builder;

import java.io.StringWriter;

import org.testng.annotations.Test;

import chain4j.AssertThread;
import chain4j.Service;

/**
 * A test of the scenarios when we will block the calling thread
 *
 * @author wassj
 *
 */
public class BlockCallingThreadOrNotTest {

	@Test
	public void block()
		throws Exception {

		final StringWriter out = new StringWriter();

		for (int i = 0; i < 10; ++i) {
			ChainBuilder.create().add(AssertThread.is(Service.t1)).executor(Service.t1.executor()).build().invoke();
		}

		System.out.println(out.toString());

		/*for (String pair : out.getBuffer().toString().split(",")) {
			final String[] split = pair.split(" ");
			Assert.assertEquals(split.length, 2, "invalid output [split array length]");
			Assert.assertEquals(split[0], split[1], "out of order");
		}*/
	}


	public void blockMultiple() {

	}

}
