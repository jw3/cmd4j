package cmd4j.examples;

import org.testng.annotations.Test;

import cmd4j.IChain;
import cmd4j.Say;
import cmd4j.common.Chains;
import cmd4j.common.Chains.ChainBuilder;

/**
 *
 *
 * @author wassj
 *
 */
public class TestHowReadableStackTracesAre {

	@Test(expectedExceptions = Exception.class)
	public void tenChainsDeep()
		throws Exception {

		final ChainBuilder builder = Chains.builder();
		this.recurse(builder, 0, 10).invoke();
	}


	private IChain recurse(ChainBuilder parent, int count, int max) {
		System.out.println("@" + count);
		if (++count < max) {
			final IChain chain = recurse(Chains.builder().add(Say.what(String.valueOf(count))), count, max);
			parent.add(chain);
		}
		return parent.add(Say.boom()).build();
	}
}
