package chain4j.fsm;

import org.testng.annotations.Test;

/**
 *
 *
 * @author wassj
 *
 */
public class StateMachineTest {

	@Test
	public void test() {
		final State print = new State() {
			public State run(Object dto) {
				System.out.println("woo hoo!");
				return new State() {
					public State run(Object dto) {
						System.out.println("woo hoo! (AGAIN)");
						return null;
					}
				};
			}
		};
		final StateMachine m = new StateMachine();
		m.setStart(print);
		m.invoke();
	}


	public void executePath() {
	}


	public void executionAbortedDueToException() {
	}


	public void executePathIgnoringException() {
	}
}
