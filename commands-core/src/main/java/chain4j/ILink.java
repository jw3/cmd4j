package chain4j;

/**
 *
 *
 * @author wassj
 *
 */
public interface ILink {

	ILink next();


	Object dto();


	ICommand cmd();


	ILink dto(final Object dto);
}
