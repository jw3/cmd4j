package cmd4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import cmd4j.IChain.IUndoChain;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.IFunction;
import cmd4j.ICommand.IInputCommand;
import cmd4j.ICommand.IPipeIO;
import cmd4j.ICommand.IReturningCommand;
import cmd4j.Internals.Builder;
import cmd4j.Internals.Chain.ChainCallable;
import cmd4j.Internals.Chain.DefaultChain.ReturningChain;
import cmd4j.Internals.Chain.UndoableChainDecorator;
import cmd4j.Internals.Chain.VisitingChainDecorator;
import cmd4j.Internals.Link;

import com.google.common.base.Supplier;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Utility methods for {@link IChain chains}
 *
 * @author wassj
 *
 */
public class Chains {

	/**
	 * creates a new {@link ChainBuilder}
	 */
	public static IChainBuilder builder() {
		return new Builder.BaseBuilder();
	}


	/**
	 * create a {@link IChain chain} that contains the given vararg {@link ICommand commands}
	 */
	public static IChain<Void> create(final ICommand... commands) {
		return create(Arrays.asList(commands));
	}


	/**
	 * create a {@link IChain chain} that contains the given {@link ICommand commands}
	 */
	public static IChain<Void> create(final Collection<ICommand> commands) {
		final IChainBuilder builder = builder();
		for (final ICommand command : commands) {
			builder.add(command);
		}
		return builder.build();
	}


	/**
	 * create a {@link IChain chain} that contains the given vararg {@link ICommand commands}
	 */
	public static <O> IChain<O> create(final IReturningCommand<O> command) {
		if (command instanceof IChain<?>) {
			return (IChain<O>)command;
		}
		return new ReturningChain<O>(Link.create(command));
	}


	/**
	 * wrap a chain with returning capability
	 * @param chain
	 * @return
	 */
	public static IChain<Object> returns(final IChain<?> chain) {
		return Chains.builder().add(chain).add(Internals.Chain.peekAt(chain)).returns().build();
	}


	/**
	 * wrap a chain with returning capability
	 * @param chain
	 * @param type
	 * @return
	 */
	public static <O> IChain<O> returns(final IChain<?> chain, final Class<O> type) {
		return Chains.builder().add(chain).add(Internals.Chain.peekAt(chain)).returns(type).build();
	}


	/**
	 * wrap a chain with returning capability
	 * @param chain
	 * @param returnFunction
	 * @return
	 */
	public static <O> IChain<O> returns(final IChain<?> chain, final IFunction<?, O> returnFunction) {
		return Chains.builder().add(chain).add(Internals.Chain.peekAt(chain)).returns(returnFunction).build();
	}


	/**
	 * decorate a {@link IChain chain} with visiting behavior
	 */
	public static <O> IChain<O> visits(final IChain<O> chain) {
		return new VisitingChainDecorator<O>(chain);
	}


	/**
	 * add undo support to a {@link IChain chain}
	 * @return the chain, decorated
	 */
	public static <O> IUndoChain<O> undoable(final IChain<O> chain) {
		if (chain instanceof IUndoChain<?>) {
			return (IUndoChain<O>)chain;
		}
		return new UndoableChainDecorator<O>(chain);
	}


	/**
	 * wrap a chain up in a {@link Callable}
	 * @param chain
	 * @return
	 */
	public static <O> Callable<O> callable(final IChain<O> chain) {
		return new ChainCallable<O>(chain);
	}


	/**
	 * wrap a {@link IChain} and input up in a {@link Callable}
	 * @param chain
	 * @param input
	 * @return
	 */
	public static <O> Callable<O> callable(final IChain<O> chain, final Object input) {
		return new ChainCallable<O>(chain, input);
	}


	/**
	 * submit a {@link IChain} to the {@link ExecutorService} returning the resulting {@link ListenableFuture}
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static <O> ListenableFuture<O> submit(final IChain<O> chain, final ExecutorService executor) {
		return MoreExecutors.listeningDecorator(executor).submit(Commands.callable(chain));
	}


	/**
	 * submit a {@link IChain} with input to the {@link ExecutorService} returning the resulting {@link ListenableFuture}
	 * @param chain
	 * @param executor
	 * @return
	 */
	public static <O> ListenableFuture<O> submit(final IChain<O> chain, final Object input, final ExecutorService executor) {
		return MoreExecutors.listeningDecorator(executor).submit(Commands.callable(chain, input));
	}


	/**
	 * base interface for a chain builder, allows returning of the active type
	 * @author wassj
	 * @param <O> return type
	 * @param <B> active type
	 */
	interface IBaseFluentChainBuilder<O, B extends IBaseFluentChainBuilder<O, ?>>
		extends Supplier<IChain<O>> {

		/**
		 * construct the chain
		 * same as {@link #get()}
		 * will deprecate build in a future version and get() will remain
		 * @return
		 */
		IChain<O> build();


		/**
		 * construct the chain
		 * (duplication of build for {@link Supplier})
		 * @return
		 */
		public IChain<O> get();


		/**
		 * add a {@link ICommand command}
		 * @param command
		 * @return
		 */
		B add(ICommand command);


		/**
		 * add a collection of commands 
		 * @param command
		 * @return
		 */
		B add(Collection<? extends ICommand> command);


		/**
		 * add a {@link Future}
		 * @param future
		 * @return
		 */
		<R> B add(Future<R> future);


		/**
		 * add a {@link IPipeIO}
		 * @return
		 */
		B pipe();


		/**
		 * add the specified {@link IPipe}
		 * @return
		 */
		//B pipe(IPipe pipe);

		/**
		 * specify an executor for the previously added command 
		 * @param executor
		 * @return
		 */
		B executor(ExecutorService executor);


		/**
		 * specify the input object for the previously added command
		 * this overrides the chain input object
		 * @param input
		 * @return
		 */
		B input(Object input);


		/**
		* set the chain to visiting mode
		* @param visits
		* @return
		*/
		B visits(boolean visits);
	}


	/**
	 * a IChain<Void> {@link IBaseFluentChainBuilder builder}
	 * @author wassj
	 *
	 */
	public interface IChainBuilder
		extends IBaseFluentChainBuilder<Void, IChainBuilder> {

		/**
		 * obtain a {@link IReturningChainBuilder builder} that specifies a return type of {@link Object} 
		 * @return
		 */
		IReturningChainBuilder<Object> returns();


		/**
		 * obtain a {@link IReturningChainBuilder builder} that specifies a return value of the provided type
		 * if the chain result is not of the specified type the chain will return null 
		 * @param type
		 * @return
		 */
		<O> IReturningChainBuilder<O> returns(final Class<O> type);


		/**
		 * obtain a {@link IReturningChainBuilder builder} that specifies a return type based on the
		 * return type of the specified {@link IReturningCommand}.  if the the specified command supports
		 * {@link IInputCommand} the chain will supply the chain result value, and the result of the
		 * specified command will be returned. 
		 * 
		 * if the command cannot run (ie the result object does not fit) the chain will return null
		 * 
		 * @param function
		 * @return
		 */
		<O> IReturningChainBuilder<O> returns(final IReturningCommand<O> command);
	}


	/**
	 * chain builder of return type O
	 * @author wassj
	 * @param <O> the return type
	 */
	public interface IReturningChainBuilder<O>
		extends IBaseFluentChainBuilder<O, IReturningChainBuilder<O>> {
	}


	/**
	 * The context in which a {@link ICommand command} executes.
	 * 
	 * Represents 'a {@link ILink link} of a {@link IChain chain}'.  In other words; a part of a chain that
	 * is potentially connected to another link that is executed prior and likewise for one that executes after.
	 * 
	 * @author wassj
	 * 
	 * @input A Link can provide an overriding input that will be passed to commands executing withing the context of this Link.
	 * 
	 * @see Links
	 */
	public interface ILink {

		/**
		 * get the {@link ILink link} to be executed after this 
		 * @return next {@link ILink link} or null
		 */
		ILink next();


		/**
		 * Data Transfer Object that is passed to {@link ICommand commands} within this link.
		 * This acts as an override to the {@link IChain chain} level input.
		 * @return Object the Data Transfer Object
		 */
		Object input();


		ILink input(Object input);


		/**
		 * the {@link ICommand command} in this link.  The actual command type will be inspected 
		 * at execution time.  It is possible that through the use of {@link ICommand3 command3}
		 * that this link could execute more than one command.  So this property should
		 * be thought of to represent the 'head' command for this link.
		 * @return {@link ICommand}
		 */
		ICommand cmd();


		ExecutorService executor();
	}


	private Chains() {
		/*noinstance*/
	}
}
