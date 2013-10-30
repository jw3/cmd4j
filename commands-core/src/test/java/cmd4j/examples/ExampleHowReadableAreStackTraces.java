package cmd4j.examples;

import cmd4j.Chains;
import cmd4j.Chains.ChainBuilder;
import cmd4j.IChain;
import cmd4j.testing.Says;

/**
 *
 *
 * @author wassj
 *
 */
public class ExampleHowReadableAreStackTraces {

	//@Test
	//(expectedExceptions = Exception.class)
	public void tenChainsDeep()
		throws Exception {

		final ChainBuilder builder = Chains.builder();
		Chains.invoke(this.recurse(builder, 0, 10));
	}


	private IChain<Void> recurse(ChainBuilder parent, int count, int max) {
		if (++count < max) {
			final IChain<Void> chain = recurse(Chains.builder().add(Says.what(String.valueOf(count))), count, max);
			parent.add(chain);
		}
		return parent.add(Says.boom()).build();
	}
}
