package chain4j.examples;

import org.testng.annotations.Test;

import chain4j.IChain;
import chain4j.Say;
import chain4j.builder.ChainBuilder;

/**
 *
 *
 * @author wassj
 *
 */
public class TestHowReadableStackTracesAre {

	@Test
	public void tenChainsDeep()
		throws Exception {

		final ChainBuilder builder = ChainBuilder.create();
		this.recurse(builder, 0, 10).invoke();
	}


	private IChain recurse(ChainBuilder parent, int count, int max) {
		System.out.println("@" + count);
		if (++count < max) {
			final IChain chain = recurse(ChainBuilder.create(Say.what(String.valueOf(count))), count, max);
			parent.add(chain);
		}
		return parent.add(Say.boom()).build();
	}
}
