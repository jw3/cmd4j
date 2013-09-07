package chain4j;

import org.testng.annotations.Test;

import chain4j.internal.Link;

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
		m.exec();
	}


	abstract public static class State
		extends Link {

		public State() {
			super(null);
		}


		public Link call()
			throws Exception {

			return run(this.dto());
		}


		abstract public State run(Object dto)
			throws Exception;
	}


	/*
	 * 
	 * 
	 */
	public static class StateMachine
		extends DynamicChain {

		public void setStart(State state) {
			this.next(state);
		}


		@Override
		public void onSuccess(final ILink result) {
			final ILink next = result != null ? result : this.next();
			this.next(null);

			if (next != null) {
				this.executeLink(next);
			}
		}
	}
}
