package chain4j;

/**
 * 
 *
 * @author wassj
 *
 */
public interface IChain {

	void run();


	ILink head();


	Object dto();


	IChain dto(final Object dto);
}
