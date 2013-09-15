package chain4j.common;

/**
 * Provides callback when execution is complete.
 *
 * @author wassj
 *
 */
public interface IDoneListener {

	/**
	 * handle a succecssful completion
	 */
	void onSuccess();


	/**
	 * handle a failure due to exception
	 * @param cause The cause of the failure
	 */
	void onException(Throwable cause);
}
