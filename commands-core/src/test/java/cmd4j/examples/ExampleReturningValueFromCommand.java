package cmd4j.examples;

import cmd4j.ICommand;
import cmd4j.ICommand.ICommand1;
import cmd4j.common.Chains;
import cmd4j.testing.Say;

/**
 *
 *
 * @author wassj
 *
 */
public class ExampleReturningValueFromCommand {

	public static void main(String[] args)
		throws Exception {

		final Returns<Integer> result = new Returns<Integer>();
		Chains.builder().add(add(1, 2, result)).add(Say.what(result)).build().invoke();
	}


	public static ICommand add(final int lhs, final int rhs, final Returns<Integer> returnValue) {
		return new ICommand1() {
			public void invoke() {
				returnValue.setValue(lhs + rhs);
			}
		};
	}


	private static class Returns<V> {
		private V value;


		public V getValue() {
			return value;
		}


		public void setValue(V value) {
			this.value = value;
		}


		@Override
		public String toString() {
			return String.valueOf(this.getValue());
		}
	}
}
