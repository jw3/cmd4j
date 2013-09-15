package chain4j.common;

import chain4j.ICommand;
import chain4j.ICommand1;
import chain4j.ILink;

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

		final ILink link = Links.create(command);
		Links.execute(link);
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
