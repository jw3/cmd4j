package chain4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import chain4j.builder.Links;
import chain4j.internal.Link;

/**
 * Basic tests of {@link DynamicChain} chain flow
 * 
 * @author wassj
 *
 */
public class TestDynamicChains {

	@Test
	public void trivialFlowOnSingleThread()
		throws InterruptedException {

		final String expectedBase = "abc";
		for (int i = 0; i < 5; ++i) {
			final StringBuilder expected = new StringBuilder();
			for (int j = 0; j < i; ++j) {
				expected.append(expectedBase);
			}

			final Fsm fsm = new Fsm(i);
			fsm.exec();
			Thread.sleep(100);
			Assert.assertEquals(fsm.buffer.toString(), expected.toString());
		}
	}


	/*
	 * 
	 * 
	 */
	private static class Fsm
		extends DynamicChain {

		final StringBuilder buffer = new StringBuilder();
		int rounds = 1;
		int count;


		public Fsm() {
			next(a());
		}


		public Fsm(int rounds) {
			this.rounds = rounds;
			final Link next = count < rounds ? a() : null;
			next(next);
		}


		private IChainable say(final String what, final Link next) {
			return new IChainable() {
				public void invoke() {
					buffer.append(what);
					next(next);
				}
			};
		}


		public Link a() {
			return Links.create(say("a", b()));
		}


		public Link b() {
			return Links.create(say("b", c()));
		}


		public Link c() {
			return Links.create(new IChainable() {
				public void invoke() {
					buffer.append("c");
					final Link next = ++count < rounds ? a() : null;
					next(next);
				}
			});
		}
	}
}
