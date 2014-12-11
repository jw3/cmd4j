package cmd4j.testing;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.annotation.Nullable;

import cmd4j.ICommand.ICommand2;

/**
 * Some test utilities
 * 
 * @author wassj
 *
 */
abstract public class Says
	implements ICommand2<Object> {

	public interface ISayFactory {
		String toString();


		Says input();


		Says threadInput();


		Says thread();


		Says what(Object what);
	}


	public static ISayFactory factory() {
		return new SayFactory();
	}


	public static Says input(final Writer... into) {
		return new Says() {
			public void invoke(@Nullable final Object input)
				throws Exception {

				if (into.length == 0) {
					System.out.println(String.valueOf(input));
				}
				else {
					for (Writer out : into) {
						out.write(String.valueOf(input));
					}
				}
			}
		};
	}


	public static Says threadInput(final Writer... into) {
		return new Says() {
			public void invoke(@Nullable final Object input)
				throws IOException {

				final StringBuilder buffer = new StringBuilder();
				buffer.append(Thread.currentThread().getName());

				if (input != null) {
					buffer.append("\t").append(String.valueOf(input));
				}
				if (into.length == 0) {
					System.out.println(buffer.toString());
				}
				else {
					for (Writer out : into) {
						out.write(buffer.toString());
					}
				}
			}
		};
	}


	public static Says thread(final Writer... into) {
		return new Says() {
			public void invoke(@Nullable final Object input)
				throws IOException {

				final StringBuilder buffer = new StringBuilder();
				buffer.append(Thread.currentThread().getName());

				if (into.length == 0) {
					System.out.println(buffer.toString());
				}
				else {
					for (Writer out : into) {
						out.write(buffer.toString());
					}
				}
			}
		};
	}


	public static Says what(final Object toSay, final Object... into) {
		return new Says() {
			public void invoke(@Nullable final Object input)
				throws IOException {

				if (into.length == 0) {
					final StringBuilder buffer = new StringBuilder();
					buffer.append("[ ").append(Thread.currentThread().getName()).append(" ]").append("\t");
					buffer.append(toSay).append(" ");
					if (input != null) {
						buffer.append(String.valueOf(input));
					}

					System.out.println(buffer.toString());
				}
				else {
					for (Object out : into) {
						if (out instanceof Writer) {
							((Writer)out).write(String.valueOf(toSay));
						}
						else if (out instanceof StringBuilder) {
							((StringBuilder)out).append(String.valueOf(toSay));
						}
					}
				}
			}
		};
	}


	/*
	 * 
	 * 
	 * 
	 */
	private static class SayFactory
		implements ISayFactory {

		private final StringWriter writer = new StringWriter();


		public Says what(final Object what) {
			return Says.what(what, writer);
		}


		public Says threadInput() {
			return Says.thread(writer);
		}


		public Says thread() {
			return Says.thread(writer);
		}


		public Says input() {
			return Says.input(writer);
		}


		@Override
		public String toString() {
			return writer.toString();
		}
	}
}
