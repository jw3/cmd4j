package cmd4j.internal;

import java.util.concurrent.ExecutorService;

/**
 * Marks an object for execution by a specified {@link ExecutorService}
 *
 * @author wassj
 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
 *
 */
public interface IThreaded {
	ExecutorService executor();
}
