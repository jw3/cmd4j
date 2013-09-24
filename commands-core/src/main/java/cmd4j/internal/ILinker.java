package cmd4j.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import cmd4j.IChain;
import cmd4j.ILink;

/**
 * The traverser of {@link ILink}s in an {@link IChain}.  It controls the execution flow from link
 * to link and ensures that each command is run by the appropriate {@link ExecutorService}.
 * 
 * @author wassj
 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
 *
 */
public interface ILinker {

	ILink head();


	IToCallable toCallable();


	ILinker toCallable(IToCallable toCallable);


	IExecutorOf executorOf();


	ILinker executorOf(IExecutorOf executorOf);


	/**
	 * functor to convert an {@link ILink} to a {@link Callable}
	 *
	 * @author wassj
	 *
	 */
	public interface IToCallable {
		Callable<ILink> get(ILink link, Object dto);
	}


	public interface IExecutorOf {
		ExecutorService get(ILink link, ExecutorService defaultIfNull);
	}
}
