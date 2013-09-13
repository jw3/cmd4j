package chain4j;

import javax.swing.SwingUtilities;

import junit.framework.Assert;

/**
 * Utility factory class for making assertions about what thread a given point in a chain is executing on
 *
 * @author wassj
 *
 */
public class AssertThread
	implements ICommand {

	private final Thread expected;


	/**
	 * Assert that the thread that creates this assertion will also execute it
	 * @return
	 */
	public static AssertThread isCurrent() {
		return new AssertThread(Thread.currentThread());
	}


	/**
	 * Assert that the passed thread is used to execute this assertion
	 */
	public static AssertThread is(final Thread thread) {
		return new AssertThread(thread);
	}


	/**
	 * Assert that this assertion is run on the Event Dispatch Thread 
	 */
	public static AssertThread isEDT() {
		return new AssertThread() {
			public void invoke() {
				Assert.assertTrue("expected to be run on EDT, was run on " + Thread.currentThread().getName(), SwingUtilities.isEventDispatchThread());
			}
		};
	}


	/*
	 * default invocation compared the current thread agains the expected thread with a TestNG assertion
	 */
	public void invoke()
		throws Exception {

		final Thread current = Thread.currentThread();
		Assert.assertEquals(expected.getId(), current.getId());
	}


	private AssertThread() {
		this.expected = null;
	}


	private AssertThread(final Thread thread) {
		this.expected = thread;
	}

}
