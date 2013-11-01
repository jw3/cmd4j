package cmd4j;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand4;
import cmd4j.testing.Does.Variable;

/**
 *
 *
 * @author wassj
 *
 */
public class VisitingTest {

	@Test
	public void test2_foo()
		throws Exception {

		final Variable<Boolean> foo = Variable.create(false);
		final Variable<Boolean> bar = Variable.create(false);
		final Variable<Boolean> foobar = Variable.create(false);

		Chains.builder().add(handleFoo2(foo)).add(handleBar2(bar)).add(handleFooBar2(foobar)).visits(true).build().invoke(new IFoo() {
		});

		foo.assertEquals(true);
		bar.assertEquals(false);
		foobar.assertEquals(false);
	}


	@Test
	public void test2_bar()
		throws Exception {

		final Variable<Boolean> foo = Variable.create(false);
		final Variable<Boolean> bar = Variable.create(false);
		final Variable<Boolean> foobar = Variable.create(false);

		Chains.builder().add(handleFoo2(foo)).add(handleBar2(bar)).add(handleFooBar2(foobar)).visits(true).build().invoke(new IBar() {
		});

		foo.assertEquals(false);
		bar.assertEquals(true);
		foobar.assertEquals(false);
	}


	@Test
	public void test2_foobar()
		throws Exception {

		final Variable<Boolean> foo = Variable.create(false);
		final Variable<Boolean> bar = Variable.create(false);
		final Variable<Boolean> foobar = Variable.create(false);

		Chains.builder().add(handleFoo2(foo)).add(handleBar2(bar)).add(handleFooBar2(foobar)).visits(true).build().invoke(new IFooBar() {
		});

		foo.assertEquals(true);
		bar.assertEquals(true);
		foobar.assertEquals(true);
	}


	@Test
	public void test4_foo()
		throws Exception {

		final Variable<Boolean> foo = Variable.create(false);
		final Variable<Boolean> bar = Variable.create(false);
		final Variable<Boolean> foobar = Variable.create(false);

		Chains.builder().add(handleFoo4(foo)).add(handleBar4(bar)).add(handleFooBar4(foobar)).visits(true).build().invoke(new IFoo() {
		});

		foo.assertEquals(true);
		bar.assertEquals(false);
		foobar.assertEquals(false);
	}


	@Test
	public void test4_bar()
		throws Exception {

		final Variable<Boolean> foo = Variable.create(false);
		final Variable<Boolean> bar = Variable.create(false);
		final Variable<Boolean> foobar = Variable.create(false);

		Chains.builder().add(handleFoo4(foo)).add(handleBar4(bar)).add(handleFooBar4(foobar)).visits(true).build().invoke(new IBar() {
		});

		foo.assertEquals(false);
		bar.assertEquals(true);
		foobar.assertEquals(false);
	}


	@Test
	public void test4_foobar()
		throws Exception {

		final Variable<Boolean> foo = Variable.create(false);
		final Variable<Boolean> bar = Variable.create(false);
		final Variable<Boolean> foobar = Variable.create(false);

		Chains.builder().add(handleFoo2(foo)).add(handleBar2(bar)).add(handleFooBar2(foobar)).visits(true).build().invoke(new IFooBar() {
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


	public ICommand handleFoo2(final Variable<Boolean> ran) {
		return new ICommand2<IFoo>() {
			public void invoke(final IFoo foo) {
				ran.setValue(true);
			}
		};
	}


	public ICommand handleBar2(final Variable<Boolean> ran) {
		return new ICommand2<IBar>() {
			public void invoke(final IBar bar) {
				ran.setValue(true);
			}
		};
	}


	public ICommand handleFooBar2(final Variable<Boolean> ran) {
		return new ICommand2<IFooBar>() {
			public void invoke(final IFooBar foo) {
				ran.setValue(true);
			}
		};
	}


	//

	public ICommand handleFoo4(final Variable<Boolean> ran) {
		return new ICommand4<IFoo, ICommand>() {
			public ICommand invoke(final IFoo foo) {
				ran.setValue(true);
				return null;
			}
		};
	}


	public ICommand handleBar4(final Variable<Boolean> ran) {
		return new ICommand4<IBar, ICommand>() {
			public ICommand invoke(final IBar bar) {
				ran.setValue(true);
				return null;
			}
		};
	}


	public ICommand handleFooBar4(final Variable<Boolean> ran) {
		return new ICommand4<IFooBar, ICommand>() {
			public ICommand invoke(final IFooBar foo) {
				ran.setValue(true);
				return null;
			}
		};
	}
}
