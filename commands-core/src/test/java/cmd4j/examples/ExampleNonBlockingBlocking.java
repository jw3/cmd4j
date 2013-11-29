package cmd4j.examples;

import javax.swing.SwingUtilities;

import junit.framework.Assert;

import org.testng.annotations.Test;

import cmd4j.Chains;
import cmd4j.Concurrency;
import cmd4j.IChain;
import cmd4j.testing.Says;
import cmd4j.testing.Services;

/**
 * Here is an example of blocking the calling thread but not actually blocking the calling thread
 * 
 * one of the major problems that occurred on the original chaining framework was the case of blocking a thread with #invokeAndWait() and
 * then the chain that is blocking tries to run something on the blocked thread, that would generally result in a deadlock.
 *
 * @author wassj
 *
 */
public class ExampleNonBlockingBlocking {

	@Test
	public void blockingCallFromEdtThatExecutesOnEdt()
		throws Exception {

		final StringBuilder buffer = new StringBuilder();

		buffer.append(0);

		// create a chain that has a couple commands and runs on the EDT
		final IChain<Void> chain1 = Chains.builder().add(Says.threadInput()).add(Says.what(4, buffer)).add(Says.what("chain 1 done")).add(Says.what(5, buffer)).build();
		final IChain<Void> chain2 = Chains.builder().add(Says.threadInput()).add(Says.what(7, buffer)).add(Says.what("chain 2 done")).add(Says.what(8, buffer)).build();
		final IChain<Void> chain3 = Chains.builder().add(Says.threadInput()).add(Says.what(14, buffer)).add(Says.what("chain 3 done")).add(Says.what(15, buffer)).build();

		buffer.append(1);

		SwingUtilities.invokeAndWait(new Runnable() {
			{
				buffer.append(2);
			}


			public void run() {
				try {
					buffer.append(3);
					Concurrency.submit(chain1, Services.edt.executor());
					buffer.append(6);
					Concurrency.submit(chain2, Services.edt.executor());
					buffer.append(9);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					buffer.append(10);
				}
			}
		});
		buffer.append(11);

		SwingUtilities.invokeAndWait(new Runnable() {
			{
				buffer.append(12);
			}


			public void run() {
				try {
					buffer.append(13);
					Concurrency.submit(chain3, Services.edt.executor());
					buffer.append(16);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					buffer.append(17);
				}
			}
		});
		buffer.append(18);

		String result = buffer.toString();
		for (int i = 0; i < 19; ++i) {
			final String current = String.valueOf(i);
			Assert.assertTrue(result.startsWith(current));
			result = result.substring(current.length(), result.length());
		}
	}
}
