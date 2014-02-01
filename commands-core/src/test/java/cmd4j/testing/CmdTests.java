package cmd4j.testing;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

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


	public static List<String> randoms(final int length, final int count) {
		final List<String> result = Lists.newArrayList();
		for (int i = 0; i < count; ++i) {
			result.add(random(length));
		}
		return result;
	}
}
