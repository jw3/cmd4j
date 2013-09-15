package chain4j.examples;

import org.testng.annotations.Test;

import chain4j.IBranch;
import chain4j.ICommand;
import chain4j.ICommand2;
import chain4j.ILink;
import chain4j.common.Links;

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

	private final IBranch configure = new IBranch() {
		public ILink invoke(Object dto) {
			current = seed = System.currentTimeMillis();
			return divide;
		}
	};

	private final IBranch divide = new IBranch() {
		public ILink invoke(Object dto) {
			++count;
			current = current / 2;
			System.out.println(current);
			return current > 10 ? divide : Links.create(print);
		}
	};

	private final ICommand print = new ICommand2() {
		public void invoke(Object dto) {
			System.out.println("trimmed from " + seed + " to " + current + " in " + count + " loops");
		}


		public void invoke() {
			this.invoke(null);
		}
	};


	public void runThisStateMachine()
		throws Exception {

		Links.execute(configure);
	}
}
