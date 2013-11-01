package cmd4j;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2;
import cmd4j.testing.Does.Variable;

/**
 *
 *
 * @author wassj
 *
 */
public class VisitingTest {

	@Test
	public void test1()
		throws Exception {

		final Variable<Boolean> foo = Variable.create(false);
		final Variable<Boolean> bar = Variable.create(false);
		final Variable<Boolean> foobar = Variable.create(false);

		Chains.builder().add(handleFoo(foo)).add(handleBar(bar)).add(handleFooBar(foobar)).visits(true).build().invoke(new IFoo() {
		});

		foo.assertEquals(true);
		bar.assertEquals(false);
		foobar.assertEquals(false);
	}


	@Test
	public void test2()
		throws Exception {

		final Variable<Boolean> foo = Variable.create(false);
		final Variable<Boolean> bar = Variable.create(false);
		final Variable<Boolean> foobar = Variable.create(false);

		Chains.builder().add(handleFoo(foo)).add(handleBar(bar)).add(handleFooBar(foobar)).visits(true).build().invoke(new IBar() {
		});

		foo.assertEquals(false);
		bar.assertEquals(true);
		foobar.assertEquals(false);
	}


	@Test
	public void test3()
		throws Exception {

		final Variable<Boolean> foo = Variable.create(false);
		final Variable<Boolean> bar = Variable.create(false);
		final Variable<Boolean> foobar = Variable.create(false);

		Chains.builder().add(handleFoo(foo)).add(handleBar(bar)).add(handleFooBar(foobar)).visits(true).build().invoke(new IFooBar() {
		});

		foo.assertEquals(true);
		bar.assertEquals(true);
		foobar.assertEquals(true);
	}


	private interface IFoo {
	}


	private interface IBar {
	}


	private interface IFooBar
		extends IFoo, IBar {
	}


	public ICommand handleFoo(final Variable<Boolean> ran) {
		return new ICommand2<IFoo>() {
			public void invoke(final IFoo foo) {
				ran.setValue(true);
			}
		};
	}


	public ICommand handleBar(final Variable<Boolean> ran) {
		return new ICommand2<IBar>() {
			public void invoke(final IBar bar) {
				ran.setValue(true);
			}
		};
	}


	public ICommand handleFooBar(final Variable<Boolean> ran) {
		return new ICommand2<IFooBar>() {
			public void invoke(final IFooBar foo) {
				ran.setValue(true);
			}
		};
	}
}
