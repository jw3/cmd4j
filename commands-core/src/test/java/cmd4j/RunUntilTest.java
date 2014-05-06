package cmd4j;

import org.testng.Assert;
import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand3;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

/**
 *
 * @author wassj
 *
 */
public class RunUntilTest {

	@Test
	public void single()
		throws Exception {

		final ICalled<Integer> c1 = called(1);
		final ICommand until = Commands.until(Lists.<ICommand> newArrayList(c1), Predicates.equalTo(1));
		Chains.create(until).invoke();
		Assert.assertTrue(c1.called());
	}


	@Test
	public void yesAndNo()
		throws Exception {

		final ICalled<Integer> c1 = called(1);
		final ICalled<Integer> c2 = called(2);
		final ICommand until = Commands.until(Lists.<ICommand> newArrayList(c1, c2), Predicates.equalTo(1));
		Chains.create(until).invoke();

		Assert.assertTrue(c1.called());
		Assert.assertFalse(c2.called());
	}


	@Test
	public void both()
		throws Exception {

		final ICalled<Integer> c1 = called(1);
		final ICalled<Integer> c2 = called(2);
		final ICommand until = Commands.until(Lists.<ICommand> newArrayList(c1, c2), Predicates.equalTo(2));
		Chains.create(until).invoke();

		Assert.assertTrue(c1.called());
		Assert.assertTrue(c2.called());
	}


	@Test
	public void mixed_yesAndNo()
		throws Exception {

		final ICalled<Integer> c1 = called(1);
		final ICalled<String> c2 = called("foo");
		final ICommand until = Commands.until(Lists.<ICommand> newArrayList(c1, c2), Predicates.equalTo(1));
		Chains.create(until).invoke();

		Assert.assertTrue(c1.called());
		Assert.assertFalse(c2.called());
	}


	@Test
	public void mixed_both()
		throws Exception {

		final ICalled<Integer> c1 = called(1);
		final ICalled<String> c2 = called("foo");
		final ICommand until = Commands.until(Lists.<ICommand> newArrayList(c1, c2), Predicates.equalTo(2));
		Chains.create(until).invoke();

		Assert.assertTrue(c1.called());
		Assert.assertTrue(c2.called());
	}


	@Test
	public void noMatch()
		throws Exception {

		final ICalled<Integer> c1 = called(1);
		final ICalled<Integer> c2 = called(2);
		final ICommand until = Commands.until(Lists.<ICommand> newArrayList(c1, c2), Predicates.equalTo(10));
		Chains.create(until).invoke();

		Assert.assertTrue(c1.called());
		Assert.assertTrue(c2.called());
	}


	@Test
	public void different()
		throws Exception {

		final ICalled<Integer> c1 = called(1);
		final ICalled<Integer> c2 = called(2);
		final ICommand until = Commands.until(Lists.<ICommand> newArrayList(c1, c2), Predicates.equalTo("different"));
		Chains.create(until).invoke();

		Assert.assertTrue(c1.called());
		Assert.assertTrue(c2.called());
	}


	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	private interface ICalled<O>
		extends ICommand3<O> {

		boolean called();
	}


	private static <T> ICalled<T> called(final T value) {
		return new ICalled<T>() {
			private boolean called;


			public boolean called() {
				return called;
			}


			public T invoke() {
				called = true;
				return value;
			}
		};
	}
}
