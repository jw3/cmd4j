package chain4j;

import com.google.common.util.concurrent.ListeningExecutorService;

/**
 *
 *
 * @author wassj
 *
 */
public interface ILink2
	extends ILink {

	ListeningExecutorService executor();
}
