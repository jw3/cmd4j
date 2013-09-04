package chain4j;

import java.util.concurrent.ExecutorService;

/**
 * 
 *
 * @author wassj
 *
 */
public interface IChain {

	void exec();


	boolean isEmpty();


	boolean isUnthreaded();


	IChain dto(final Object dto);


	IChain executor(final ExecutorService executor);
}
