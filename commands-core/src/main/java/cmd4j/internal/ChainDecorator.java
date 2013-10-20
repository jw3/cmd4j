package cmd4j.internal;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cmd4j.IChain;
import cmd4j.ICommand;
import cmd4j.ILink;
import cmd4j.common.Chains;
import cmd4j.common.Executors2;
import cmd4j.common.Links.IThreaded;
import cmd4j.internal.Callables.UndoToCallable;
import cmd4j.internal.Callables.VisitableToCallable;
import cmd4j.internal.Linkers.ILinker;

/**
 * A dynamic decorator that allows for stacking of decorations on a {@link IChain chain}.
 * 
 * Supports passing a {@link ChainDecorator chain decorator} instance as the chain arg in the ctor.
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

	private final List<ICommand> beforeHandlers = new LinkedList<ICommand>();
	private final List<ICommand> successHandlers = new LinkedList<ICommand>();
	private final List<ICommand> failureHandlers = new LinkedList<ICommand>();
	private final List<ICommand> finishedHandlers = new LinkedList<ICommand>();
	private final List<ICommand2<ILinker>> operations = new LinkedList<ICommand2<ILinker>>();

	private final IChain chain;


	public ChainDecorator(final IChain chain) {
		this.chain = chain;
	}


	public ChainDecorator addDecoration(final ICommand2<ILinker> decoration) {
		operations.add(decoration);
		return this;
	}


	public ChainDecorator before(final ICommand... commands) {
		beforeHandlers.addAll(Arrays.asList(commands));
		return this;
	}


	public ChainDecorator onFinished(final ICommand... commands) {
		finishedHandlers.addAll(Arrays.asList(commands));
		return this;
	}


	public ChainDecorator onSuccess(final ICommand... commands) {
		successHandlers.addAll(Arrays.asList(commands));
		return this;
	}


	public ChainDecorator onFailure(final ICommand... commands) {
		failureHandlers.addAll(Arrays.asList(commands));
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

		executeHandlers(beforeHandlers, dto);

		if (finishedHandlers.isEmpty() && successHandlers.isEmpty() && failureHandlers.isEmpty()) {
			Executors2.sameThreadExecutor().submit(Callables.linker(linker, dto)).get();
		}
		else {
			try {
				Executors2.sameThreadExecutor().submit(Callables.linker(linker, dto)).get();
				executeHandlers(successHandlers, dto);
			}
			catch (ExecutionException e) {
				executeHandlers(failureHandlers, e.getCause());
				throw e;
			}
			catch (InterruptedException e) {
				executeHandlers(failureHandlers, e);
				throw e;
			}
			finally {
				executeHandlers(finishedHandlers, dto);
			}
		}
	}


	private void executeHandlers(final List<ICommand> commands, final Object dto) {
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
}
