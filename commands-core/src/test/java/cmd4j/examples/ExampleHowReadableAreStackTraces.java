package cmd4j.examples;

import org.testng.annotations.Test;

import cmd4j.Chains;
import cmd4j.Chains.ChainBuilder;
import cmd4j.IChain;
import cmd4j.testing.Does;

/**
 *
 *
 * @author wassj
 *
 */
public class ExampleHowReadableAreStackTraces {

	@Test(expectedExceptions = Exception.class)
	public void tenChainsDeep()
		throws Exception {

		final ChainBuilder builder = Chains.builder();
		this.recurse(builder, 0, 10).invoke();
	}


	private IChain<Void> recurse(ChainBuilder parent, int count, int max) {
		if (++count < max) {
			final IChain<Void> chain = recurse(Chains.builder().add(Does.nothing()), count, max);
			parent.add(chain);
		}
		return parent.add(Does.boom()).build();
	}
}
