package chain4j.common;

/**
 *
 *
 * @author wassj
 *
 */
public interface ILinkListener {

	void onSuccess();


	void onException(Throwable cause);
}
