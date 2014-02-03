package cmd4j.testing;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.testng.Assert;

import cmd4j.Commands;
import cmd4j.Commands.Variable;
import cmd4j.ICommand;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.IReturningCommand;

import com.google.common.base.Preconditions;

/**
 * Cmd4j Test Utils
 *
 * @author wassj
 *
 */
public enum Does {
	/*singleton-enum*/;

	public static IReturningCommand<Void> nothing() {
		return Commands.nop();
	}


	public static Says boom() {
		return new Says() {
			public void invoke(final Object input)
				throws Exception {

				throw new Boom();
			}
		};
	}


	public static ICommand submits(final IReturningCommand<Void> command, final ExecutorService executor) {
		return new ICommand2<Object>() {
			public void invoke(final Object input)
				throws Exception {

				final Callable<Void> callable = Commands.callable(command, input);
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
				Preconditions.checkArgument(!v.isNull(), "variable not initialized");
				v.set(!v.get());
			}
		};
	}


	public static ICommand increment(final TestVariable<Integer> v) {
		return new ICommand1() {
			public void invoke() {
				Preconditions.checkArgument(!v.isNull(), "variable not initialized");
				v.set(v.get() + 1);
			}
		};
	}


	public static <T> IReturningCommand<Void> set(final TestVariable<T> v, final T value) {
		return new ICommand1() {
			public void invoke() {
				v.set(value);
			}
		};
	}


	public static <T> IReturningCommand<Void> set(final TestVariable<T> v) {
		return new ICommand2<T>() {
			public void invoke(final T value) {
				v.set(value);
			}
		};
	}


	public static <R> IReturningCommand<R> returnsNull() {
		return new ICommand3<R>() {
			public R invoke() {
				return null;
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
		return new UndoableSetter1<T>(v, value);
	}


	public static <T> ICommand undoableSet2(final TestVariable<T> v, final T value) {
		return new UndoableSetter2<T>(v, value);
	}


	private abstract static class BaseUndodoableSetter<T> {
		protected final T original;
		protected final T modified;
		protected final TestVariable<T> var;


		public BaseUndodoableSetter(final TestVariable<T> var, final T modified) {
			this.original = var.get();
			this.modified = modified;
			this.var = var;
		}
	}


	private static class UndoableSetter1<T>
		extends BaseUndodoableSetter<T>
		implements ICommand1, ICommand1.IUndo {

		public UndoableSetter1(final TestVariable<T> var, final T modified) {
			super(var, modified);
		}


		public void invoke() {
			var.set(modified);
		}


		public void undo() {
			var.set(original);
		}
	}


	private static class UndoableSetter2<T>
		extends BaseUndodoableSetter<T>
		implements ICommand2<Object>, ICommand2.IUndo<Object> {

		public UndoableSetter2(final TestVariable<T> var, final T modified) {
			super(var, modified);
		}


		public void invoke(Object o) {
			var.set(modified);
		}


		public void undo(Object o) {
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


	public static class Boom
		extends Exception {

		public Boom() {
			super("boom");
		}
	}
}
