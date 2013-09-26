package cmd4j.testing;

import java.util.concurrent.ExecutorService;

/**
 *
 *
 * @author wassj
 *
 */
public interface IService {

	String name();


	boolean isOwnerOfCurrentThread();


	ExecutorService executor();
}
