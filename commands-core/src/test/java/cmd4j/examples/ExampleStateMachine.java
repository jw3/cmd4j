package cmd4j.examples;

import cmd4j.Chains;
import cmd4j.ICommand;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;

/**
 * Example of using {@link IBranch} to create states with dynamic flow.
 * Thus a Finite State Machine requires no special consideration, as demonstrated below.
 *
 * @author wassj
 *
 */
public class ExampleStateMachine {

	public static void main(String[] args)
		throws Exception {

		new ExampleStateMachine().runThisStateMachine();
	}

	/*
	 * impl
	 */

	private long seed;
	private long current;
	private long count;

	private final ICommand3<ICommand> configure = new ICommand3<ICommand>() {
		public ICommand invoke() {
			current = seed = System.currentTimeMillis();
			return divide;
		}
	};

	private final ICommand3<ICommand> divide = new ICommand3<ICommand>() {
		public ICommand invoke() {
			++count;
			current = current / 2;
			System.out.println(current);
			return current > 10 ? divide : print;
		}
	};

	private final ICommand print = new ICommand2<Object>() {
		public void invoke(Object input) {
			System.out.println("trimmed from " + seed + " to " + current + " in " + count + " loops");
		}
	};


	public void runThisStateMachine()
		throws Exception {

		Chains.create(configure).invoke();
	}
}
