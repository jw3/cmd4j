package cmd4j.common;

import cmd4j.ICommand;
import cmd4j.ICommand1;
import cmd4j.ICommand2;
import cmd4j.ICommand3;

/**
 * Utility methods for {@link ICommand commands}
 *
 * @author wassj
 *
 */
public enum Commands {
	/*singleton-enum*/;

	public static <T> ICommand tokenizeType(Class<T> type, ICommand2<T> command) {
		return new Command2Proxy<T>(command, type);
	}


	public static <T> ICommand tokenizeType(Class<T> type, ICommand3<T> command) {
		return new Command3Proxy<T>(command, type);
	}


	/**
	 * execute the specified {@link ICommand command}
	 * @throws Exception
	 */
	public static void execute(ICommand command)
		throws Exception {

		Chains.builder().add(command).build().invoke();
	}


	/**
	 * provide a no-operation {@link ICommand command}
	 * @return Command that does nothing
	 */
	public static ICommand nop() {
		return new ICommand1() {
			public void invoke() {
			}
		};
	}


	/**
	 * Provide the means of storing the acceptable dto type on a command,
	 * useful as a workaround to erasure.
	 *
	 * @author wassj
	 */
	public interface ICommandProxy<T>
		extends ICommand {

		Class<T> type();


		ICommand command();
	}


	private static class Command2Proxy<T>
		implements ICommandProxy<T>, ICommand2<T> {

		private final Class<T> type;
		private final ICommand2<T> command;


		public Command2Proxy(final ICommand2<T> command, final Class<T> type) {
			this.command = command;
			this.type = type;
		}


		public Class<T> type() {
			return type;
		}


		public ICommand command() {
			return command;
		}


		public void invoke(final T dto)
			throws Exception {

			command.invoke(dto);
		}
	}


	private static class Command3Proxy<T>
		implements ICommandProxy<T>, ICommand3<T> {

		private final Class<T> type;
		private final ICommand3<T> command;


		public Command3Proxy(final ICommand3<T> command, final Class<T> type) {
			this.command = command;
			this.type = type;
		}


		public Class<T> type() {
			return type;
		}


		public ICommand command() {
			return command;
		}


		public ICommand invoke(final T dto)
			throws Exception {

			return command.invoke(dto);
		}
	}
}
