package cmd4j;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand4;
import cmd4j.testing.Asserts;

/**
 *
 *
 * @author wassj
 *
 */
public class SwapsIoTest {

	@Test
	public void test()
		throws Exception {

		Chains.builder().add(Asserts.is(100)).add(intToString()).ioSwap().add(Asserts.is("100")).build().invoke(100);
	}


	public ICommand4<Integer, String> intToString() {
		return new ICommand4<Integer, String>() {
			public String invoke(final Integer input) {
				return String.valueOf(input);
			}
		};
	}
}
