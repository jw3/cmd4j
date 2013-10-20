package cmd4j.testing;

import org.testng.Assert;

import cmd4j.Chains;
import cmd4j.Commands;
import cmd4j.ICommand;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;

/**
 * Cmd4j Test Utils
 *
 * @author wassj
 *
 */
public enum Tests {
	/*singleton-enum*/;

	public static ICommand invoked(final Variable<Boolean> called) {
		return new ICommand2() {
			public void invoke(final Object dto) {
				called.setValue(true);
			}
		};
	}


	public static <T> ICommand invoked(final Class<T> type, final Variable<Boolean> called) {
		return Commands.tokenize(type, new ICommand2<T>() {
			public void invoke(final T dto) {
				called.setValue(true);
			}
		});
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


	public static <T> ICommand set(final Variable<T> v, final T value) {
		return new ICommand1() {
			public void invoke() {
				v.setValue(value);
			}
		};
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
