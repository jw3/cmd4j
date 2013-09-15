package chain4j;

import java.util.concurrent.Callable;

/**
 *
 *
 * @author wassj
 *
 */
public interface ILink
	extends Callable<ILink> {

	ILink next();


	Object dto();


	ICommand cmd();


	ILink dto(final Object dto);
}
