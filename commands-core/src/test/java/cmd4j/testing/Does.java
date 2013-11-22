package cmd4j.testing;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.testng.Assert;

import cmd4j.Commands.Variable;
import cmd4j.Concurrent;
import cmd4j.ICommand;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.IReturningCommand;

/**
 * Cmd4j Test Utils
 *
 * @author wassj
 *
 */
public enum Does {
	/*singleton-enum*/;

	public static ICommand1 nothing() {
		return new ICommand1() {
			public void invoke() {
			}
		};
	}


	public static Says boom() {
		return new Says() {
			public void invoke(final Object input)
				throws Exception {

				throw new Exception("boom");
			}
		};
	}


	public static ICommand submits(final IReturningCommand<Void> command, final ExecutorService executor) {
		return new ICommand2<Object>() {
			public void invoke(final Object input)
				throws Exception {

				final Callable<Void> callable = Concurrent.asCallable(command, input);
				executor.submit(callable).get();
			}
		};
	}


	public static ICommand invoked(final TestVariable<Boolean> called) {
		return new ICommand2<Object>() {
			public void invoke(final Object input) {
				called.set(true);
			}
		};
	}


	public static <T> TestVariable<T> var(final T val) {
		return new TestVariable<T>(val);
	}


	public static ICommand toggle(final TestVariable<Boolean> v) {
		return new ICommand1() {
			public void invoke() {
				if (v.get() == null) {
					throw new NullPointerException("variable was not initialized");
				}
				v.set(!v.get());
			}
		};
	}


	public static ICommand add(final TestVariable<Integer> v, final int amount) {
		return new ICommand1() {
			public void invoke() {
				if (v.get() == null) {
					throw new NullPointerException("variable was not initialized");
				}
				v.set(v.get() + amount);
				System.out.println(v.get());
			}
		};
	}


	public static <T> ICommand set(final TestVariable<T> v, final T value) {
		return new ICommand1() {
			public void invoke() {
				v.set(value);
			}
		};
	}


	public static <T> ICommand set(final TestVariable<T> v) {
		return new ICommand2<T>() {
			public void invoke(final T value) {
				v.set(value);
			}
		};
	}


	public static <R> IReturningCommand<R> returns(final R val) {
		return new ICommand3<R>() {
			public R invoke() {
				return val;
			}
		};
	}


	public static <R> IReturningCommand<R> returns(final TestVariable<R> val) {
		return new ICommand3<R>() {
			public R invoke() {
				return val.get();
			}
		};
	}


	public static <T> ICommand undoableSet(final TestVariable<T> v, final T value) {
		return new UndodoableSetter<T>(v, value);
	}


	private static class UndodoableSetter<T>
		implements ICommand1, ICommand1.IUndo {

		private final T original;
		private final T modified;
		private final TestVariable<T> var;


		public UndodoableSetter(final TestVariable<T> var, final T modified) {
			this.original = var.get();
			this.modified = modified;
			this.var = var;
		}


		public void invoke() {
			var.set(modified);
		}


		public void undo() {
			var.set(original);
		}
	}


	/**
	 * 
	 *
	 *
	 * @author wassj
	 *
	 * @param <T>
	 */
	public static class TestVariable<T>
		extends Variable<T> {

		public static <T> TestVariable<T> create() {
			return new TestVariable<T>();
		}


		public static <T> TestVariable<T> create(final T value) {
			return new TestVariable<T>(value);
		}


		public TestVariable() {
		}


		public TestVariable(final T value) {
			super(value);
		}


		public void assertEquals(final T expected) {
			Assert.assertEquals(get(), expected);
		}


		public void assertNotEquals(final T expected) {
			Assert.assertNotEquals(get(), expected);
		}
	}
}
