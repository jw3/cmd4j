package chain4j.fsm;

import org.testng.annotations.Test;

import chain4j.fsm.State;
import chain4j.fsm.StateMachine;

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
		m.run();
	}
}
