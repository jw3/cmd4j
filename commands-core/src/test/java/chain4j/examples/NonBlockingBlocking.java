package chain4j.examples;

import javax.swing.SwingUtilities;

import junit.framework.Assert;

import org.testng.annotations.Test;

import chain4j.IChain;
import chain4j.Say;
import chain4j.Service;
import chain4j.builder.ChainBuilder;

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
		final IChain chain1 = ChainBuilder.create().add(Say.thread()).add(Say.what(4, buffer)).add(Say.what("chain 1 done")).add(Say.what(5, buffer)).build(Service.edt.get());
		final IChain chain2 = ChainBuilder.create().add(Say.thread()).add(Say.what(7, buffer)).add(Say.what("chain 2 done")).add(Say.what(8, buffer)).build(Service.edt.get());
		final IChain chain3 = ChainBuilder.create().add(Say.thread()).add(Say.what(14, buffer)).add(Say.what("chain 3 done")).add(Say.what(15, buffer)).build(Service.edt.get());

		buffer.append(1);

		SwingUtilities.invokeAndWait(new Runnable() {
			{
				buffer.append(2);
			}


			public void run() {
				try {
					buffer.append(3);
					chain1.invoke();
					buffer.append(6);
					chain2.invoke();
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
					chain3.invoke();
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
