package chain4j;

/**
 * 
 *
 * @author wassj
 *
 */
public interface IChain
	extends ICommand2<Object> {

	ILink head();
}
