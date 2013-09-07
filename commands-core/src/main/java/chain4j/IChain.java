package chain4j;

/**
 * 
 *
 * @author wassj
 *
 */
public interface IChain {

	void exec();


	ILink head();


	Object dto();


	IChain dto(final Object dto);
}
