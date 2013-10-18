package cmd4j.common;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import cmd4j.ICommand;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;

/**
 * Utility methods for {@link ICommand commands}
 *
 * @author wassj
 *
 */
public enum Commands {
	/*singleton-enum*/;
	/**
	 * execute the specified {@link ICommand command}
	 * @throws Exception
	 */
	public static void execute(ICommand command)
		throws Exception {

		Chains.builder().add(command).build().invoke();
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


	/**
	 * wrap a future in a command
	 * @param future
	 * @return
	 */
	public static ICommand future(final Future<?> future) {
		return new ICommand1() {
			public void invoke()
				throws Exception {
				future.get();
			}
		};
	}


	/**
	 * wrap a callable in a command
	 * @param future
	 * @return
	 */
	public static ICommand callable(final Callable<?> callable) {
		return new ICommand1() {
			public void invoke()
				throws Exception {
				callable.call();
			}
		};
	}


	/**
	 * tokenize the type parameter of the command
	 * useful only when erasure is removing necessary type information
	 * @param type
	 * @param command
	 * @return
	 */
	public static <T> ICommand tokenize(Class<T> type, ICommand2<T> command) {
		return new Command2Proxy<T>(command, type);
	}


	/**
	 * tokenize the type parameter of the command
	 * useful only when erasure is removing necessary type information
	 * @param type
	 * @param command
	 * @return
	 */
	public static <T> ICommand tokenize(Class<T> type, ICommand3<T> command) {
		return new Command3Proxy<T>(command, type);
	}


	/**
	 * Provide the means of storing the acceptable dto type on a command, useful as a workaround to erasure.
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
