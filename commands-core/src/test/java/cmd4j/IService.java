package cmd4j;

import java.util.concurrent.ExecutorService;

/**
 *
 *
 * @author wassj
 *
 */
public interface IService {

	String name();


	boolean isCurrent();


	ExecutorService executor();
}
