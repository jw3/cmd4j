package chain4j;

import java.util.concurrent.Callable;

import com.google.common.util.concurrent.ListeningExecutorService;

/**
 *
 *
 * @author wassj
 *
 */
public interface ILink
	extends Callable<ILink>, Iterable<ICommand> {

	ILink next();


	Object dto();


	ILink dto(final Object dto);


	ListeningExecutorService executor();
}
