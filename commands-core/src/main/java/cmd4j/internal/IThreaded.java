package cmd4j.internal;

import java.util.concurrent.ExecutorService;

import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Marks an object for execution by a specified {@link ListeningExecutorService}
 *
 * @author wassj
 * @internal Intended for Command Framework use only.  Unsafe for direct client usage.
 *
 */
public interface IThreaded {
	ExecutorService executor();
}
