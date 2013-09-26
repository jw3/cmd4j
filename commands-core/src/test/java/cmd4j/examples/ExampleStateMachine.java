package cmd4j.examples;

import org.testng.annotations.Test;

import cmd4j.ICommand;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.common.Commands;

/**
 * Example of using {@link IBranch} to create states with dynamic flow.
 * Thus a Finite State Machine requires no special consideration, as demonstrated below.
 *
 * @author wassj
 *
 */
public class ExampleStateMachine {

	@Test
	public void test()
		throws Exception {

		new ExampleStateMachine().runThisStateMachine();
	}

	/*
	 * impl
	 */

	private long seed;
	private long current;
	private long count;

	private final ICommand3 configure = new ICommand3() {
		public ICommand invoke(Object dto) {
			current = seed = System.currentTimeMillis();
			return divide;
		}
	};

	private final ICommand3 divide = new ICommand3() {
		public ICommand invoke(Object dto) {
			++count;
			current = current / 2;
			System.out.println(current);
			return current > 10 ? divide : print;
		}
	};

	private final ICommand print = new ICommand2() {
		public void invoke(Object dto) {
			System.out.println("trimmed from " + seed + " to " + current + " in " + count + " loops");
		}
	};


	public void runThisStateMachine()
		throws Exception {

		Commands.execute(configure);
	}
}
