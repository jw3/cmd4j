package cmd4j;

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
}
