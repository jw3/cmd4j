package cmd4j.examples;

import javax.swing.SwingUtilities;

import junit.framework.Assert;

import org.testng.annotations.Test;

import cmd4j.Chains;
import cmd4j.IChain;
import cmd4j.testing.Say;
import cmd4j.testing.Service;

/**
 * Here is an example of blocking the calling thread but not actually blocking the calling thread
 * 
 * one of the major problems that occurred on the original chaining framework was the case of blocking a thread with #invokeAndWait() and
 * then the chain that is blocking tries to run something on the blocked thread, that would generally result in a deadlock.
 *
 * @author wassj
 *
 */
public class NonBlockingBlocking {

	@Test
	public void blockingCallFromEdtThatExecutesOnEdt()
		throws Exception {

		final StringBuilder buffer = new StringBuilder();

		buffer.append(0);

		// create a chain that has a couple commands and runs on the EDT
		final IChain chain1 = Chains.builder().add(Say.threadDto()).add(Say.what(4, buffer)).add(Say.what("chain 1 done")).add(Say.what(5, buffer)).build();
		final IChain chain2 = Chains.builder().add(Say.threadDto()).add(Say.what(7, buffer)).add(Say.what("chain 2 done")).add(Say.what(8, buffer)).build();
		final IChain chain3 = Chains.builder().add(Say.threadDto()).add(Say.what(14, buffer)).add(Say.what("chain 3 done")).add(Say.what(15, buffer)).build();

		buffer.append(1);

		SwingUtilities.invokeAndWait(new Runnable() {
			{
				buffer.append(2);
			}


			public void run() {
				try {
					buffer.append(3);
					Chains.submit(chain1, Service.edt.executor());
					buffer.append(6);
					Chains.submit(chain2, Service.edt.executor());
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
					Chains.submit(chain3, Service.edt.executor());
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
		System.out.println(result);

		for (int i = 0; i < 19; ++i) {
			final String current = String.valueOf(i);
			Assert.assertTrue(result.startsWith(current));
			result = result.substring(current.length(), result.length());
			System.out.println(i + " = " + current);
		}
	}
}
