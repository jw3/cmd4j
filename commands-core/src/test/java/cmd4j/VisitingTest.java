package cmd4j;

import java.util.concurrent.ExecutionException;

import org.testng.Assert;
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
	public void test_String()
		throws Exception {

		final Variable<Boolean> strings = Variable.create(false);
		final Variable<Boolean> integers = Variable.create(false);
		final Variable<Boolean> numbers = Variable.create(false);
		final Variable<Boolean> objects = Variable.create(false);

		Chains.builder().add(handleObject2(objects)).add(handleString2(strings)).add(handleInteger2(integers)).add(handleNumber2(numbers)).visits(true).build().invoke("string");

		objects.assertEquals(true);
		strings.assertEquals(true);
		integers.assertEquals(false);
		numbers.assertEquals(false);

		// validate that without visits, the chain fails
		Throwable cause = null;
		try {
			Chains.builder().add(handleObject2(objects)).add(handleString2(strings)).add(handleInteger2(integers)).add(handleNumber2(numbers)).visits(false).build().invoke("string");
		}
		catch (ExecutionException e) {
			cause = e.getCause();
		}
		catch (Exception e) {
			cause = e;
		}
		Assert.assertTrue(cause instanceof IllegalArgumentException);
	}


	@Test
	public void test_Integer()
		throws Exception {

		final Variable<Boolean> strings = Variable.create(false);
		final Variable<Boolean> integers = Variable.create(false);
		final Variable<Boolean> numbers = Variable.create(false);
		final Variable<Boolean> objects = Variable.create(false);

		Chains.builder().add(handleObject2(objects)).add(handleString2(strings)).add(handleInteger2(integers)).add(handleNumber2(numbers)).visits(true).build().invoke(Integer.MAX_VALUE);

		objects.assertEquals(true);
		strings.assertEquals(false);
		integers.assertEquals(true);
		numbers.assertEquals(true);

		// validate that without visits, the chain fails
		Throwable cause = null;
		try {
			Chains.builder().add(handleObject2(objects)).add(handleString2(strings)).add(handleInteger2(integers)).add(handleNumber2(numbers)).visits(false).build().invoke(Integer.MAX_VALUE);
		}
		catch (ExecutionException e) {
			cause = e.getCause();
		}
		catch (Exception e) {
			cause = e;
		}
		Assert.assertTrue(cause instanceof IllegalArgumentException);
	}


	@Test
	public void test_Number()
		throws Exception {

		final Variable<Boolean> strings = Variable.create(false);
		final Variable<Boolean> integers = Variable.create(false);
		final Variable<Boolean> numbers = Variable.create(false);
		final Variable<Boolean> objects = Variable.create(false);

		Chains.builder().add(handleObject2(objects)).add(handleString2(strings)).add(handleInteger2(integers)).add(handleNumber2(numbers)).visits(true).build().invoke(1.3f);

		objects.assertEquals(true);
		strings.assertEquals(false);
		integers.assertEquals(false);
		numbers.assertEquals(true);

		// validate that without visits, the chain fails
		Throwable cause = null;
		try {
			Chains.builder().add(handleObject2(objects)).add(handleString2(strings)).add(handleInteger2(integers)).add(handleNumber2(numbers)).visits(false).build().invoke(1.3f);
		}
		catch (ExecutionException e) {
			cause = e.getCause();
		}
		catch (Exception e) {
			cause = e;
		}
		Assert.assertTrue(cause instanceof IllegalArgumentException);
	}


	@Test
	public void test_Object()
		throws Exception {

		final Variable<Boolean> strings = Variable.create(false);
		final Variable<Boolean> integers = Variable.create(false);
		final Variable<Boolean> numbers = Variable.create(false);
		final Variable<Boolean> objects = Variable.create(false);

		Chains.builder().add(handleObject2(objects)).add(handleString2(strings)).add(handleInteger2(integers)).add(handleNumber2(numbers)).visits(true).build().invoke(new Object());

		objects.assertEquals(true);
		strings.assertEquals(false);
		integers.assertEquals(false);
		numbers.assertEquals(false);

		// validate that without visits, the chain fails
		Throwable cause = null;
		try {
			Chains.builder().add(handleObject2(objects)).add(handleString2(strings)).add(handleInteger2(integers)).add(handleNumber2(numbers)).visits(false).build().invoke(new Object());
		}
		catch (ExecutionException e) {
			cause = e.getCause();
		}
		catch (Exception e) {
			cause = e;
		}
		Assert.assertTrue(cause instanceof IllegalArgumentException);
	}


	//

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

		// validate that without visits, the chain fails
		Throwable cause = null;
		try {
			Chains.builder().add(handleFoo2(foo)).add(handleBar2(bar)).add(handleFooBar2(foobar)).visits(false).build().invoke(new IFoo() {
			});
		}
		catch (ExecutionException e) {
			cause = e.getCause();
		}
		catch (Exception e) {
			cause = e;
		}
		Assert.assertTrue(cause instanceof IllegalArgumentException);
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

		// validate that without visits, the chain fails
		Throwable cause = null;
		try {
			Chains.builder().add(handleFoo2(foo)).add(handleBar2(bar)).add(handleFooBar2(foobar)).visits(false).build().invoke(new IBar() {
			});
		}
		catch (ExecutionException e) {
			cause = e.getCause();
		}
		catch (Exception e) {
			cause = e;
		}
		Assert.assertTrue(cause instanceof IllegalArgumentException);
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

		// validate that without visits, the chain fails
		Throwable cause = null;
		try {
			Chains.builder().add(handleFoo4(foo)).add(handleBar4(bar)).add(handleFooBar4(foobar)).visits(false).build().invoke(new IFoo() {
			});

		}
		catch (ExecutionException e) {
			cause = e.getCause();
		}
		catch (Exception e) {
			cause = e;
		}
		Assert.assertTrue(cause instanceof IllegalArgumentException);
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

		// validate that without visits, the chain fails
		Throwable cause = null;
		try {
			Chains.builder().add(handleFoo4(foo)).add(handleBar4(bar)).add(handleFooBar4(foobar)).visits(false).build().invoke(new IBar() {
			});
		}
		catch (ExecutionException e) {
			cause = e.getCause();
		}
		catch (Exception e) {
			cause = e;
		}
		Assert.assertTrue(cause instanceof IllegalArgumentException);
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


	public ICommand handleObject2(final Variable<Boolean> ran) {
		return new ICommand2<Object>() {
			public void invoke(final Object object) {
				ran.setValue(true);
			}
		};
	}


	public ICommand handleString2(final Variable<Boolean> ran) {
		return new ICommand2<String>() {
			public void invoke(final String string) {
				ran.setValue(true);
			}
		};
	}


	public ICommand handleInteger2(final Variable<Boolean> ran) {
		return new ICommand2<Integer>() {
			public void invoke(final Integer integer) {
				ran.setValue(true);
			}
		};
	}


	public ICommand handleNumber2(final Variable<Boolean> ran) {
		return new ICommand2<Number>() {
			public void invoke(final Number number) {
				ran.setValue(true);
			}
		};
	}


	//

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
