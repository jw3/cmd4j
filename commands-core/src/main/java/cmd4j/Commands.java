package cmd4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IPipeIO;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.ICommand.IStateCommand.IStateCommand2;
import cmd4j.Internals.Builder.BaseBuilder;
import cmd4j.Internals.Builder.ReturningBuilder;
import cmd4j.Internals.Command.CommandCallable;
import cmd4j.Internals.Command.RunOneCallFactory;
import cmd4j.Internals.Executor.EventDispatchExecutor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * General {@link ICommand} related utilities
 *
 * @author wassj
 *
 */
public class Commands {

	/**
	 * no-operation command
	 * @return
	 */
	public static ICommand nop() {
		return new ICommand1() {
			public void invoke() {
			}
		};
	}


	/**
	 * command that returns the passed value
	 * @param output
	 * @return
	 */
	public static <O> IReturningCommand<O> returns(final O output) {
		return new ICommand3<O>() {
			public O invoke() {
				return output;
			}
		};
	}


	/**
	 * an io pipe, aka |, directs the previous output to the next input
	 * @see IPipeIO
	 * @return
	 */
	public static <I> IPipeIO<I> pipe() {
		return new IPipeIO<I>() {
			public I invoke(final I input) {
				return input;
			}
		};
	}


	/**
	 * {@link ICommand command} that will wait for a specified number of {@link TimeUnit#MILLISECONDS milliseconds}
	 * @param timeout
	 * @return
	 */
	public static ICommand waitFor(final long timeout) {
		return waitFor(timeout, TimeUnit.MILLISECONDS);
	}


	/**
	 * {@link ICommand command} that will wait for a specified number of specified {@link TimeUnit time unit}
	 * @param timeout
	 * @return
	 */
	public static ICommand waitFor(final long timeout, final TimeUnit unit) {
		return new ICommand1() {
			public void invoke()
				throws Exception {

				new CountDownLatch(1).await(timeout, unit);
			}
		};
	}


	/**
	 * wait on the {@link CountDownLatch} to open
	 * @param latch
	 * @return Command that is blocked until latch is released
	 */
	public static ICommand waitFor(final CountDownLatch latch) {
		return new ICommand1() {
			public void invoke()
				throws Exception {

				latch.await();
			}
		};
	}


	/**
	 * count down the {@link CountDownLatch}
	 * @param latch
	 * @return
	 */
	public static ICommand countDown(final CountDownLatch latch) {
		return new ICommand1() {
			public void invoke() {
				latch.countDown();
			}
		};
	}


	/**
	 * wrap a {@link Future} up in a {@link IReturningCommand}
	 * @param future
	 * @return
	 */
	public static <O> IReturningCommand<O> from(final Future<O> future) {
		return new ICommand3<O>() {
			public O invoke()
				throws Exception {

				return future.get();
			}
		};
	}


	/**
	 * wrap a {@link Callable} up in a {@link IReturningCommand}
	 * @param future
	 * @return
	 */
	public static <O> IReturningCommand<O> from(final Callable<O> callable) {
		return new ICommand3<O>() {
			public O invoke()
				throws Exception {

				return callable.call();
			}
		};
	}


	/**
	 * wrap a {@link IReturningCommand} up in a {@link Callable}
	 * @param command
	 * @return
	 */
	public static <O> Callable<O> callable(final IReturningCommand<O> command) {
		return new CommandCallable<O>(command);
	}


	/**
	 * wrap a {@link IReturningCommand} and input up in a {@link Callable}
	 * @param command
	 * @return
	 */
	public static <O> Callable<O> callable(final IReturningCommand<O> command, final Object input) {
		return new CommandCallable<O>(command, input);
	}


	/**
	 * returns a new instance of an {@link ExecutorService} that submits tasks to the Swing Event Dispatch Thread
	 * @return
	 */
	public static ExecutorService swingExecutor() {
		return new EventDispatchExecutor();
	}


	/**
	 * returns a {@link ICommand} that schedules the passed {@link ExecutorService} for shutdown
	 * @param executor
	 * @return
	 */
	public static ICommand shutdownExecutor(final ExecutorService executor) {
		return new ICommand1() {
			public void invoke() {
				executor.shutdown();
			}
		};
	}


	/**
	 * wrap a Guava {@link Function} in a {@link ICommand4}
	 * @param function
	 * @return
	 */
	public static <I, O> ICommand4<I, O> from(final Function<I, O> function) {
		return new ICommand4<I, O>() {
			public O invoke(final I input) {
				return function.apply(input);
			}
		};
	}


	/**
	 * wrap a {@link ICommand} with a Guava {@link Function}
	 * @param command
	 * @return
	 */
	public static <I, O> Function<I, O> asFunction(final ICommand4<I, O> command) {
		return new Function<I, O>() {
			public O apply(final I input) {
				try {
					return command.invoke(input);
				}
				catch (final Exception e) {
					throw new RuntimeException("command invocation failed", e);
				}
			}
		};
	}


	/**
	 * invoke the {@link ICommand} if the {@link Predicate} applies to the input object
	 * @param command
	 * @param condition
	 * @return
	 */
	public static <I> IStateCommand2<I> invokeIf(final ICommand command, final Predicate<I> condition) {
		return new IStateCommand2<I>() {
			public ICommand invoke(final I input) {
				return condition.apply(input) ? command : null;
			}
		};
	}


	/**
	 * execute no more than one of the passed commands
	 * @param commands
	 * @return
	 */
	public static ICommand onlyOne(final ICommand... commands) {
		return onlyOne(Arrays.asList(commands));
	}


	/**
	 * execute no more than one of the passed commands
	 * @param commands
	 * @return
	 */
	public static ICommand onlyOne(final Collection<? extends ICommand> commands) {
		final BaseBuilder builder = new BaseBuilder();
		builder.add(commands);
		return new ReturningBuilder<Object>(builder).build(new RunOneCallFactory<Object>());
	}


	/**
	 * util class for passing values as parameter
	 * @author wassj
	 */
	public static class Variable<T> {
		private T value;
		private Integer hashCode;


		public Variable() {
		}


		/**
		 * set the value of this variable
		 * @param value
		 */
		public Variable(final T value) {
			this.value = value;
		}


		/**
		 * get the value of this variable
		 * @return
		 */
		public T get() {
			return value;
		}


		/**
		 * set the value of this variable
		 * @param value
		 */
		public void set(final T value) {
			this.value = value;
		}


		/**
		 * test the value against null
		 * @return
		 */
		public boolean isNull() {
			return null == value;
		}


		@Override
		public String toString() {
			return String.valueOf(value);
		}


		@Override
		public boolean equals(final Object other) {
			if (this == other) return true;
			// danger! if (isNull() && null == other) return true;

			if (other instanceof Variable<?>) {
				final Variable<?> otherVar = (Variable<?>)other;
				return isNull() ? otherVar.isNull() : value.equals(otherVar.value);
			}
			return false;
		}


		@Override
		public int hashCode() {
			if (hashCode == null) {
				if (!isNull()) {
					return -31 * value.hashCode();
				}
				hashCode = super.hashCode();
			}
			return hashCode;
		}
	}


	private Commands() {
		/*noinstance*/
	}
}
