package cmd4j;

import java.util.Collection;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.Internals.Command.ReturnVoidWrapper;

/**
 * Utility methods for {@link ICommand commands}
 *
 * @author wassj
 *
 */
public enum Commands {
	/*singleton-enum*/;

	/**
	 * wrap up a command in a {@link IReturningCommand}, discarding the return behavior
	 * @param command
	 * @return
	 */
	public static IReturningCommand<Void> voidWrap(final ICommand command) {
		return new ReturnVoidWrapper(command);
	}


	/**
	 * execute the specified {@link ICommand command}
	 * @throws Exception
	 */
	public static void invoke(final ICommand command)
		throws Exception {

		invoke(new ReturnVoidWrapper(command), null);
	}


	/**
	 * execute the specified {@link ICommand command}
	 * @throws Exception
	 */
	public static <R> R invoke(final IReturningCommand<R> command)
		throws Exception {

		return invoke(command, null);
	}


	/**
	 * execute the specified {@link ICommand command}
	 * @throws Exception
	 */
	public static <R> R invoke(final IReturningCommand<R> command, final Object dto)
		throws Exception {

		return Chains.invoke(Chains.create(command));
	}


	public static void invoke(final Collection<ICommand> commands)
		throws Exception {

		invoke(commands, null);
	}


	public static void invoke(final Collection<ICommand> commands, final Object dto)
		throws Exception {

		Chains.invoke(Chains.create(commands), dto);
	}


	/**
	 * create an empty (no-operation) {@link ICommand command}
	 * @return Command that does nothing
	 */
	public static ICommand nop() {
		return new ICommand1() {
			public void invoke() {
			}
		};
	}
}
