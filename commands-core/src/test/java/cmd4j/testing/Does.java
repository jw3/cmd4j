package cmd4j.testing;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.testng.Assert;

import cmd4j.Chains;
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


	public static ICommand submits(final IReturningCommand<Void> command, final ExecutorService executor) {
		return new ICommand2<Object>() {
			public void invoke(final Object dto)
				throws Exception {

				final Callable<Void> callable = Concurrent.asCallable(command, dto);
				executor.submit(callable).get();
			}
		};
	}


	public static ICommand invoked(final Variable<Boolean> called) {
		return new ICommand2<Object>() {
			public void invoke(final Object dto) {
				called.setValue(true);
			}
		};
	}


	public static <T> Variable<T> var(final T val) {
		return new Variable<T>(val);
	}


	public static <T> ICommand is(final Variable<T> v, final T value) {
		return new ICommand1() {
			public void invoke() {
				Assert.assertEquals(value, v.getValue());
			}
		};
	}


	/**
	 * test the dto against the passed value
	 */
	public static <T> ICommand is(final T value) {
		final Variable<Boolean> invoked = var(false);
		return Chains.builder()//
			.add(new ICommand2<T>() {
				public void invoke(final T dto) {
					invoked.setValue(true);
					Assert.assertEquals(value, dto);
				}
			})
			.add(is(invoked, true))
			.build();
	}


	public static ICommand toggle(final Variable<Boolean> v) {
		return new ICommand1() {
			public void invoke() {
				if (v.getValue() == null) {
					throw new NullPointerException("variable was not initialized");
				}
				v.setValue(!v.getValue());
			}
		};
	}


	public static ICommand add(final Variable<Integer> v, final int amount) {
		return new ICommand1() {
			public void invoke() {
				if (v.getValue() == null) {
					throw new NullPointerException("variable was not initialized");
				}
				v.setValue(v.getValue() + amount);
				System.out.println(v.getValue());
			}
		};
	}


	public static <T> ICommand set(final Variable<T> v, final T value) {
		return new ICommand1() {
			public void invoke() {
				v.setValue(value);
			}
		};
	}


	public static <T> ICommand set(final Variable<T> v) {
		return new ICommand2<T>() {
			public void invoke(final T value) {
				v.setValue(value);
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


	public static <R> IReturningCommand<R> returns(final Variable<R> val) {
		return new ICommand3<R>() {
			public R invoke() {
				return val.getValue();
			}
		};
	}


	public static <T> ICommand undoableSet(final Variable<T> v, final T value) {
		return new UndodoableSetter<T>(v, value);
	}


	private static class UndodoableSetter<T>
		implements ICommand1, ICommand1.IUndo {

		private final T original;
		private final T modified;
		private final Variable<T> var;


		public UndodoableSetter(final Variable<T> var, final T modified) {
			this.original = var.getValue();
			this.modified = modified;
			this.var = var;
		}


		public void invoke() {
			var.setValue(modified);
		}


		public void undo() {
			var.setValue(original);
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
	public static class Variable<T> {
		private T value;


		public static <T> Variable<T> create() {
			return new Variable<T>();
		}


		public static <T> Variable<T> create(final T value) {
			return new Variable<T>(value);
		}


		public Variable() {
		}


		public Variable(final T value) {
			this.value = value;
		}


		public T getValue() {
			return value;
		}


		public void setValue(T value) {
			this.value = value;
		}


		public boolean isNull() {
			return null == value;
		}


		public void assertEquals(final T expected) {
			Assert.assertEquals(value, expected);
		}


		public void assertNotEquals(final T expected) {
			Assert.assertNotEquals(value, expected);
		}
	}
}
