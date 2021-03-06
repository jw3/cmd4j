package cmd4j;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import cmd4j.Chains.IChainBuilder;
import cmd4j.Chains.PropogatingFutureCallback;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IInputCommand;
import cmd4j.ICommand.IInvokable;
import cmd4j.ICommand.IPipeIO;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.ICommand.IStateCommand.IStateCommand1;
import cmd4j.ICommand.IStateCommand.IStateCommand2;
import cmd4j.Internals.Builder.BaseBuilder;
import cmd4j.Internals.Builder.ReturningBuilder;
import cmd4j.Internals.Command.CommandCallable;
import cmd4j.Internals.Command.CommandRunnable;
import cmd4j.Internals.Command.RunUntilCallFactory;
import cmd4j.Internals.Executor.EventDispatchExecutor;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * General {@link ICommand} related utilities
 *
 * @author wassj
 *
 */
public class Commands {

	/**
	 * invoke the specified {@link ICommand} converting any resulting exception to a {@link Runtime} exception 
	 * @param command
	 */
	public static void uncheckedInvoke(final ICommand command) {
		uncheckedInvoke(command, null);
	}


	/**
	 * invoke the specified {@link ICommand} with the specified input converting any resulting exception to a {@link Runtime} exception
	 * @param command
	 * @param input
	 */
	public static void uncheckedInvoke(final ICommand command, final Object input) {
		try {
			Chains.create(command).invoke(input);
		}
		catch (final Exception e) {
			throw Throwables.propagate(e);
		}
	}


	/**
	 * invoke the specified {@link IReturningCommand} converting any resulting exception to a {@link Runtime} exception 
	 * @param command
	 * @return
	 */
	public static <O> O uncheckedInvoke(final IReturningCommand<O> command) {
		return uncheckedInvoke(command, null);
	}


	/**
	 * invoke the specified {@link IReturningCommand} with the specified input converting any resulting exception to a {@link Runtime} exception
	 * @param command
	 * @param input
	 * @return
	 */
	public static <O> O uncheckedInvoke(final IReturningCommand<O> command, final Object input) {
		try {
			return Chains.create(command).invoke(input);
		}
		catch (final Exception e) {
			throw Throwables.propagate(e);
		}
	}


	/**
	 * no-operation command
	 * @return
	 */
	public static IReturningCommand<Void> nop() {
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
	public static <O> IReturningCommand<O> returns(@Nullable final O output) {
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

			@SuppressWarnings("unused" /*keeping this around for later use*/)
			private boolean timedout;


			public void invoke()
				throws Exception {

				timedout = !new CountDownLatch(1).await(timeout, unit);
			}
		};
	}


	/**
	 * wait on the {@link CountDownLatch} to open
	 * @param latch
	 * @return Command that is blocked until latch is released
	 */
	public static ICommand waitFor(final CountDownLatch latch) {
		Preconditions.checkNotNull(latch);
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
		Preconditions.checkNotNull(latch);
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
		Preconditions.checkNotNull(future);
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
		Preconditions.checkNotNull(callable);
		return new ICommand3<O>() {
			public O invoke()
				throws Exception {

				return callable.call();
			}
		};
	}


	/**
	 * {@link ICommand} to {@link Runnable} wrapper
	 * @param command
	 * @return
	 */
	public static Runnable runnable(final ICommand command) {
		return new CommandRunnable(command);
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
	public static <O> Callable<O> callable(final IReturningCommand<O> command, @Nullable final Object input) {
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
	 * invoke the {@link ICommand} if the {@link Predicate} applies to the input object
	 * @param command
	 * @param condition
	 * @return
	 */
	public static <I> IStateCommand2<I> invokeIf(final ICommand command, final Predicate<I> condition) {
		return invokeIf(command, condition, null);
	}


	/**
	 * invoke the {@link ICommand} if the {@link Predicate} applies to the input object, invoke the onElse if not
	 * @param command
	 * @param onElse
	 * @param condition
	 * @return
	 */
	public static <I> IStateCommand2<I> invokeIf(final ICommand command, final Predicate<I> condition, @Nullable final ICommand onElse) {
		return new IStateCommand2<I>() {
			public ICommand invoke(@Nullable final I input) {
				return condition.apply(input) ? command : onElse;
			}
		};
	}


	/**
	 * execute the commands in order until the condition is met
	 * the specified {@link Predicate} will check the return value of each command for satisfaction
	 * @param commands
	 * @param condition
	 * @return
	 */
	public static ICommand until(final Collection<? extends ICommand> commands, final Predicate<? extends Object> condition) {
		final BaseBuilder builder = new BaseBuilder();
		builder.add(commands);
		return new ReturningBuilder<Object>(builder).build(new RunUntilCallFactory<Object>(condition));
	}


	/**
	 * execute the command until the condition is met
	 * the specified {@link Predicate} will check the return value for satisfaction
	 * @param command
	 * @param condition
	 * @return
	 */
	public static ICommand until(final ICommand command, final Predicate<? extends Object> condition) {
		return until(Lists.newArrayList(command), condition);
	}


	/**
	 * Execute the command for each object provided through {@link IInputCommand}
	 * @param command
	 * @return
	 */
	public static ICommand forEach(final ICommand command) {
		return forEach(Suppliers.ofInstance(command));
	}


	/**
	 * Execute the command for each of the objects in inputs
	 * @param inputs
	 * @param command
	 * @return
	 */
	public static <I> IInvokable<Void> forEach(final Collection<I> inputs, final ICommand command) {
		return forEach(inputs, Suppliers.ofInstance(command));
	}


	/**
	 * Same function as {@link #forEach(Collection, Supplier)} with the input Collection being supplied through {@link IInputCommand}
	 * @param supplier
	 * @return
	 */
	public static ICommand forEach(final Supplier<? extends ICommand> supplier) {
		return new IStateCommand2<Collection<?>>() {
			public ICommand invoke(final Collection<?> input) {
				return forEach(input, supplier);
			}
		};
	}


	/**
	 * Execute the command supplied by the supplier for each object with inputs; 
	 * @param inputs
	 * @param supplier
	 * @return
	 */
	public static <I> IInvokable<Void> forEach(final Collection<I> inputs, final Supplier<? extends ICommand> supplier) {
		final IChainBuilder builder = Chains.builder();
		for (final I input : inputs) {
			builder.add(supplier.get()).input(input);
		}
		return builder.build();
	}


	/**
	 * submit a {@link IReturningCommand} to the {@link ExecutorService} returning the resulting {@link ListenableFuture}
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static <O> ListenableFuture<O> submit(final IReturningCommand<O> command, final ExecutorService executor) {
		return submit(command, null, executor);
	}


	public static ListenableFuture<Void> submit(final ICommand command, final ExecutorService executor) {
		return submit(Chains.create(command), executor);
	}


	/**
	 * submit a {@link IReturningCommand} with input to the {@link ExecutorService} returning the resulting {@link ListenableFuture}
	 * @param command
	 * @param executor
	 * @return
	 */
	public static <O> ListenableFuture<O> submit(final IReturningCommand<O> command, @Nullable final Object input, final ExecutorService executor) {
		final ListenableFuture<O> future = MoreExecutors.listeningDecorator(executor).submit(Commands.callable(command, input));
		Futures.addCallback(future, new PropogatingFutureCallback<O>());
		return future;
	}


	public static ListenableFuture<Void> submit(final ICommand command, @Nullable final Object input, final ExecutorService executor) {
		return submit(Chains.create(command), input, executor);
	}


	/**
	 * create a command that will submit a {@link IReturningCommand} to the {@link ExecutorService} returning the resulting {@link ListenableFuture}
	 * the input value can be passed through the input object
	 * @param command
	 * @param executor
	 * @return
	 */
	public static <O> IReturningCommand<ListenableFuture<O>> submitLater(final IReturningCommand<O> command, final ExecutorService executor) {
		return new ICommand4<Object, ListenableFuture<O>>() {
			public ListenableFuture<O> invoke(@Nullable final Object input) {
				return submit(command, input, executor);
			}
		};
	}


	/**
	 * create a command that will submit a {@link IReturningCommand} to the {@link ExecutorService} returning the resulting {@link ListenableFuture}
	 * @param command
	 * @param input
	 * @param executor
	 * @return
	 */
	public static <O> IReturningCommand<ListenableFuture<O>> submitLater(final IReturningCommand<O> command, @Nullable final Object input, final ExecutorService executor) {
		return new ICommand3<ListenableFuture<O>>() {
			public ListenableFuture<O> invoke() {
				return submit(command, input, executor);
			}
		};
	}


	/**
	 * @author wassj
	 */
	public static class ForwardingCommand
		implements IStateCommand1 {

		private ICommand command;


		public ForwardingCommand() {
		}


		public ForwardingCommand(final ICommand command) {
			this.command = command;
		}


		public ICommand getCommand() {
			return command;
		}


		public void setCommand(ICommand command) {
			this.command = command;
		}


		public ICommand invoke() {
			Preconditions.checkState(this != command, "cannot forward self");
			return command;
		}
	}


	/**
	 * util class for passing values as parameter
	 * @author wassj
	 */
	public static class Variable<T> {
		private T value;
		private Integer hashCode;


		public static <T> Variable<T> create() {
			return new Variable<T>();
		}


		public static <T> Variable<T> create(final T value) {
			return new Variable<T>(value);
		}


		public Variable() {
		}


		/**
		 * set the value of this variable
		 * @param value
		 */
		public Variable(@Nullable final T value) {
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
		public void set(@Nullable final T value) {
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
		public boolean equals(@Nullable final Object other) {
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
					return hashCode = -31 * value.hashCode();
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
