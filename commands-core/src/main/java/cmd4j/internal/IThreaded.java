package cmd4j.internal;

import java.util.concurrent.ExecutorService;

import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Marks an object for execution by a specified {@link ListeningExecutorService}
 *
 * @author wassj
 *
 */
public interface IThreaded {
	ExecutorService executor();
}
