package cmd4j.common;

import cmd4j.ICommand;
import cmd4j.ICommand1;

/**
 * {@link ICommand} utilities
 *
 * @author wassj
 *
 */
public enum Commands {
	/*singleton-enum*/;

	/**
	 * execute the specified {@link ICommand}
	 * @param command
	 * @throws Exception
	 */
	public static void execute(ICommand command)
		throws Exception {

		Chains.builder().add(command).build().invoke();
	}


	/**
	 * provide a no-operation {@link ICommand}
	 * @return Command that does nothing
	 */
	public static ICommand nop() {
		return new ICommand1() {
			public void invoke() {
			}
		};
	}
}
