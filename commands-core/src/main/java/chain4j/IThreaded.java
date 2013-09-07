package chain4j;

import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Marks an object for execution by a specified {@link ListeningExecutorService}
 *
 * @author wassj
 *
 */
public interface IThreaded {

	ListeningExecutorService executor();
}
