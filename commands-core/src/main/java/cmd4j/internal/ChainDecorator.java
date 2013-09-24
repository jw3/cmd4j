package cmd4j.internal;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.ICommand2;
import cmd4j.ILink;
import cmd4j.common.Chains;
import cmd4j.common.ExecutorServices;
import cmd4j.common.Links.IThreaded;
import cmd4j.internal.Callables.UndoToCallable;
import cmd4j.internal.Callables.VisitableToCallable;

/**
 * A dynamic decorator that allows for stacking of decorations on an {@link IChain}.
 * 
 * Supports passing a {@link ChainDecorator} instance as the chain arg in the ctor.
 * Implements {@link IThreaded} to provide threading decoration support.
 * 
 * @note Using the Command API to implement a feature for itself.
 *
 * @author wassj
 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
 *
 */
public class ChainDecorator
	implements IChain {

	private final List<ICommand2<ILinker>> operations = new LinkedList<ICommand2<ILinker>>();
	private final List<ICommand> successHandlers = new LinkedList<ICommand>();
	private final List<ICommand> failureHandlers = new LinkedList<ICommand>();

	private final IChain chain;

	private ExecutorService executor;


	public ChainDecorator(final IChain chain) {
		this(chain, null);
	}


	public ChainDecorator(final IChain chain, final ExecutorService executor) {
		this.chain = chain;
		this.executor = executor;
	}


	public ChainDecorator addDecoration(final ICommand2<ILinker> decoration) {
		operations.add(decoration);
		return this;
	}


	public ChainDecorator addSuccessHandlers(final ICommand... commands) {
		successHandlers.addAll(Arrays.asList(commands));
		return this;
	}


	public ChainDecorator addFailureHandlers(final ICommand... commands) {
		failureHandlers.addAll(Arrays.asList(commands));
		return this;
	}


	public ExecutorService executor() {
		if (executor == null) {
			executor = ExecutorServices.sameThreadExecutor();
		}
		return executor;
	}


	public ChainDecorator executor(ExecutorService executor) {
		this.executor = executor;
		return this;
	}


	/*
	 * 
	 * IChain impl
	 * 
	 */
	public ILink head() {
		return chain.head();
	}


	public void invoke()
		throws Exception {

		this.invoke((Object)null);
	}


	public void invoke(final Object dto)
		throws Exception {

		final ILinker linker = Linkers.create(this.head());
		for (ICommand2<ILinker> operation : operations) {
			operation.invoke(linker);
		}
		if (successHandlers.isEmpty() && failureHandlers.isEmpty()) {
			this.executor.submit(Callables.linker(linker, dto)).get();
		}
		else {
			try {
				this.executor.submit(Callables.linker(linker, dto)).get();
				handleCompletion(successHandlers, dto);
			}
			catch (ExecutionException e) {
				handleCompletion(failureHandlers, e.getCause());
			}
			catch (InterruptedException e) {
				handleCompletion(failureHandlers, e);
			}
		}
	}


	private void handleCompletion(final List<ICommand> commands, final Object dto) {
		try {
			Chains.create(commands).invoke(dto);
		}
		catch (Throwable t) {
			// REVISIT the show must go on
			t.printStackTrace();
		}
	}


	/*
	 * 
	 * operations
	 * 
	 * 
	 */
	public ChainDecorator visitable() {
		return this.addDecoration(new Visitable());
	}


	public ChainDecorator undo() {
		return this.addDecoration(new Undo());
	}


	public ChainDecorator unthreaded() {
		return this.addDecoration(new Unthreaded());
	}


	private static class Visitable
		implements ICommand2<ILinker> {

		public void invoke(final ILinker linker)
			throws Exception {

			linker.setToCallable(new VisitableToCallable());
		}
	}


	private static class Undo
		implements ICommand2<ILinker> {

		public void invoke(final ILinker linker)
			throws Exception {

			linker.setToCallable(new UndoToCallable());
		}
	}


	private static class Unthreaded
		implements ICommand2<ILinker> {

		public void invoke(final ILinker linker)
			throws Exception {

			Linkers.makeUnthreaded(linker);
		}
	}
}
