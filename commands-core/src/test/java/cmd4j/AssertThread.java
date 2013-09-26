package cmd4j;

import org.testng.Assert;

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


	public static AssertThread is(final IService service) {
		return new AssertThread() {
			public void invoke() {
				Assert.assertTrue(service.isOwnerOfCurrentThread(), "expected to be run on " + service.name() + ", was run on " + Thread.currentThread().getName());
			}
		};
	}


	/**
	 * Assert that this assertion is run on the Event Dispatch Thread 
	 */
	public static AssertThread isEDT() {
		return is(Service.edt);
	}


	/*
	 * default invocation compared the current thread agains the expected thread with a TestNG assertion
	 */
	public void invoke()
		throws Exception {

		final Thread current = Thread.currentThread();
		Assert.assertEquals(this.getExpected().getId(), current.getId());
	}


	AssertThread() {
		this.expected = null;
	}


	AssertThread(final Thread thread) {
		this.expected = thread;
	}


	public Thread getExpected() {
		return expected;
	}
}
