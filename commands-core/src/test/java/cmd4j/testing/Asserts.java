package cmd4j.testing;

import org.testng.Assert;

import cmd4j.Chains;
import cmd4j.ICommand;
import cmd4j.testing.Does.Variable;

/**
 * Utility factory class for making assertions about what thread a given point in a chain is executing on
 *
 * @author wassj
 *
 */
public class Asserts
	implements ICommand {

	private final Thread expected;


	public static <T> ICommand is(final Variable<T> v, final T value) {
		return new ICommand1() {
			public void invoke() {
				Assert.assertEquals(value, v.getValue());
			}
		};
	}


	/**
	* test the input against the passed value
	*/

	public static <T> ICommand is(final T value) {
		final Variable<Boolean> invoked = Does.var(false);
		return Chains.builder()//
			.add(new ICommand2<T>() {
				public void invoke(final T input) {
					invoked.setValue(true);
					Assert.assertEquals(value, input);
				}
			})
			.add(is(invoked, true))
			.build();
	}


	/**
	 * Assert that the thread that creates this assertion will also execute it
	 * @return
	 */
	public static Asserts isCurrent() {
		return new Asserts(Thread.currentThread());
	}


	/**
	 * Assert that the passed thread is used to execute this assertion
	 */
	public static Asserts isRunningIn(final Thread thread) {
		return new Asserts(thread);
	}


	public static Asserts isRunningIn(final IService service) {
		return new Asserts() {
			public void invoke() {
				Assert.assertTrue(service.isOwnerOfCurrentThread(), "expected to be run on " + service.name() + ", was run on " + Thread.currentThread().getName());
			}
		};
	}


	public static <T> ICommand isEquals(final Variable<T> var, final T val) {
		return new ICommand1() {
			public void invoke() {
				Assert.assertEquals(val, var.getValue());
			}
		};
	}


	/**
	 * Assert that this assertion is run on the Event Dispatch Thread 
	 */
	public static Asserts isEDT() {
		return isRunningIn(Services.edt);
	}


	/*
	 * default invocation compared the current thread agains the expected thread with a TestNG assertion
	 */
	public void invoke()
		throws Exception {

		final Thread current = Thread.currentThread();
		Assert.assertEquals(this.getExpected().getId(), current.getId());
	}


	Asserts() {
		this.expected = null;
	}


	Asserts(final Thread thread) {
		this.expected = thread;
	}


	public Thread getExpected() {
		return expected;
	}
}
