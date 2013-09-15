package chain4j.common;

/**
 *
 *
 * @author wassj
 *
 */
public interface ICompletionHandler {

	void onSuccess();


	void onFailure(Throwable cause);
}
