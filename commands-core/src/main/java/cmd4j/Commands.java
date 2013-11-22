package cmd4j;

import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.IReturningCommand;

/**
 * General {@link ICommand} related utilities
 *
 * @author wassj
 *
 */
public class Commands {

	/**
	 * no-operation command
	 * @return
	 */
	public static ICommand nop() {
		return new ICommand1() {
			public void invoke() {
			}
		};
	}


	public static <O> IReturningCommand<O> returns(final O output) {
		return new ICommand3<O>() {
			public O invoke() {
				return output;
			}
		};
	}


	/**
	 * util class for passing values as parameter
	 * @author wassj
	 */
	public static class Variable<T> {
		private T value;
		private Integer hashCode;


		public Variable() {
		}


		/**
		 * set the value of this variable
		 * @param value
		 */
		public Variable(final T value) {
			this.value = value;
		}


		/**
		 * get the value of this variable
		 * @return
		 */
		public T get() {
			return value;
		}


		/**
		 * set the value of this variable
		 * @param value
		 */
		public void set(final T value) {
			this.value = value;
		}


		/**
		 * test the value against null
		 * @return
		 */
		public boolean isNull() {
			return null == value;
		}


		@Override
		public String toString() {
			return String.valueOf(value);
		}


		@Override
		public boolean equals(final Object other) {
			if (this == other) return true;
			// danger! if (isNull() && null == other) return true;

			if (other instanceof Variable<?>) {
				final Variable<?> otherVar = (Variable<?>)other;
				return isNull() ? otherVar.isNull() : value.equals(otherVar.value);
			}
			return false;
		}


		@Override
		public int hashCode() {
			if (hashCode == null) {
				if (!isNull()) {
					return -31 * value.hashCode();
				}
				hashCode = super.hashCode();
			}
			return hashCode;
		}
	}


	private Commands() {
		/*noinstance*/
	}
}
