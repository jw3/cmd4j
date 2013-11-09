package cmd4j;

import cmd4j.ICommand.ICommand1;

/**
 * Utility methods for {@link ICommand commands}
 *
 * @author wassj
 *
 */
public enum Commands {
	/*singleton-enum*/;

	/**
	 * create an empty (no-operation) {@link ICommand command}
	 * @return Command that does nothing
	 */
	public static ICommand1 nop() {
		return new ICommand1() {
			public void invoke() {
			}
		};
	}
}
