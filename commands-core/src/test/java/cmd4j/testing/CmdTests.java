package cmd4j.testing;

import java.util.UUID;

/**
 *
 * @author wassj
 *
 */
public class CmdTests {

	public static String random() {
		return UUID.randomUUID().toString();
	}


	public static String random(final int length) {
		final String random = random();
		return random.substring(0, Math.min(length, random.length()));
	}
}
