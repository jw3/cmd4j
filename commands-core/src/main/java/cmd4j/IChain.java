package cmd4j;

/**
 * 
 *
 * @author wassj
 *
 */
public interface IChain
	extends ICommand1, ICommand2<Object> {

	ILink head();
}
