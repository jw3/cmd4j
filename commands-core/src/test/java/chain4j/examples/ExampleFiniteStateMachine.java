package chain4j.examples;

import org.testng.annotations.Test;

import chain4j.fsm.State;
import chain4j.fsm.StateMachine;

/**
 *
 *
 * @author wassj
 *
 */
public class ExampleFiniteStateMachine
	extends StateMachine {

	@Test
	public void test() {
		new ExampleFiniteStateMachine().invoke();
	}

	/*
	 * impl
	 */

	private long seed;
	private long current;
	private long count;


	public ExampleFiniteStateMachine() {
		this.setStart(configure);
	}

	private final State configure = new State() {
		public State run(Object dto) {
			current = seed = System.currentTimeMillis();
			return divide;
		}
	};

	private final State divide = new State() {
		public State run(Object dto) {
			++count;
			current = current / 2;
			System.out.println(current);
			return current > 10 ? divide : print;
		}
	};

	private final State print = new State() {
		public State run(Object dto) {
			System.out.println("trimmed from " + seed + " to " + current + " in " + count + " loops");
			return null;
		}
	};

}
