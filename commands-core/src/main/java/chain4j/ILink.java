package chain4j;

import java.util.concurrent.Callable;

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
}
