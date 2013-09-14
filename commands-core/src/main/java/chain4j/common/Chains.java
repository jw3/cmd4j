package chain4j.common;

import chain4j.IChain;

/**
 * Utility methods for {@link IChain}s
 *
 * @author wassj
 *
 */
public enum Chains {
	/*singleton-enum*/;

	public static void invokeQuietly(final IChain chain)
		throws RuntimeException {

		try {
			chain.invoke();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static void invokeQuietly(final IChain chain, Object dto)
		throws RuntimeException {

		try {
			chain.invoke(dto);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
