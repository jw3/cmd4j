package cmd4j.examples;

import cmd4j.IChain;
import cmd4j.common.Chains;
import cmd4j.common.Chains.ChainBuilder;
import cmd4j.testing.Say;

/**
 *
 *
 * @author wassj
 *
 */
public class TestHowReadableStackTracesAre {

	//@Test
	//(expectedExceptions = Exception.class)
	public void tenChainsDeep()
		throws Exception {

		final ChainBuilder builder = Chains.builder();
		this.recurse(builder, 0, 10).invoke();
	}


	private IChain recurse(ChainBuilder parent, int count, int max) {
		if (++count < max) {
			final IChain chain = recurse(Chains.builder().add(Say.what(String.valueOf(count))), count, max);
			parent.add(chain);
		}
		return parent.add(Say.boom()).build();
	}
}
