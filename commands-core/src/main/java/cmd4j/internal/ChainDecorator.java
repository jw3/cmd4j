package cmd4j.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ICommand2;
import cmd4j.ILink;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * A dynamic decorator that allows for stacking of decorations on an {@link IChain}.
 * 
 * Supports passing a ChainDecorator instance as the Chain in the ctor.
 * Implements {@link IThreaded} to provide threading decoration support.
 * 
 * @note Use the Command API to implement a feature within itself.
 *
 * @author wassj
 *
 */
public class ChainDecorator
	implements IChain, IThreaded {

	private final List<ICommand2<Linker>> operations = new LinkedList<ICommand2<Linker>>();
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
			this.executor = executor != null ? MoreExecutors.listeningDecorator(executor) : null;
		}
	}


	public ChainDecorator add(final ICommand2<Linker> operation) {
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
			executor = MoreExecutors.sameThreadExecutor();
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

		final Linker linker = Linker.create(this.head(), dto);
		for (ICommand2<Linker> operation : operations) {
			operation.invoke(linker);
		}
		this.executor.submit(linker).get();
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


	private static class Visitable
		implements ICommand2<Linker> {

		public void invoke(Linker linker)
			throws Exception {

			linker.visitable(true);
		}
	}


	private static class Undo
		implements ICommand2<Linker> {

		public void invoke(Linker linker)
			throws Exception {

			linker.undo(true);
		}
	}
}
