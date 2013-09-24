package cmd4j.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ICommand2;
import cmd4j.ILink;
import cmd4j.common.CmdExecutors;
import cmd4j.common.Links.IThreaded;
import cmd4j.internal.CmdCallables.UndoToCallable;
import cmd4j.internal.CmdCallables.VisitableToCallable;

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
	private final IChain chain;

	private ExecutorService executor;


	public ChainDecorator(final IChain chain) {
		this(chain, null);
	}


	public ChainDecorator(final IChain chain, final ExecutorService executor) {
		if (chain instanceof ChainDecorator) {
			final ChainDecorator decorator = (ChainDecorator)chain;
			this.chain = decorator.chain;
			this.operations.addAll(decorator.operations);
			this.executor = decorator.executor;
		}
		else {
			this.chain = chain;
			this.executor = executor != null ? executor : null;
		}
	}


	public ChainDecorator add(final ICommand2<ILinker> operation) {
		operations.add(operation);
		return this;
	}


	/*
	 * 
	 * IThreaded impl
	 * 
	 * 
	 */
	public ExecutorService executor() {
		if (executor == null) {
			executor = CmdExecutors.sameThreadExecutor();
		}
		return executor;
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


	public void invoke(Object dto)
		throws Exception {

		final ILinker linker = Linkers.create(this.head());
		for (ICommand2<ILinker> operation : operations) {
			operation.invoke(linker);
		}
		this.executor.submit(CmdCallables.linker(linker, dto)).get();
	}


	/*
	 * 
	 * operations
	 * 
	 * 
	 */
	public ChainDecorator visitable() {
		return this.add(new Visitable());
	}


	public ChainDecorator undo() {
		return this.add(new Undo());
	}


	public ChainDecorator unthreaded() {
		return this.add(new Unthreaded());
	}


	private static class Visitable
		implements ICommand2<ILinker> {

		public void invoke(final ILinker linker)
			throws Exception {

			linker.toCallable(new VisitableToCallable());
		}
	}


	private static class Undo
		implements ICommand2<ILinker> {

		public void invoke(final ILinker linker)
			throws Exception {

			linker.toCallable(new UndoToCallable());
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
