package cmd4j.testing;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

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


		Says nothing();


		Says dto();


		Says threadDto();


		Says thread();


		Says what(Object what);


		Says boom();
	}


	public static ISayFactory factory() {
		return new SayFactory();
	}


	public static Says nothing() {
		return new Says() {
			public void invoke(final Object dto) {
			}
		};
	}


	public static Says dto(final Writer... into) {
		return new Says() {
			public void invoke(final Object dto)
				throws Exception {

				if (into.length == 0) {
					System.out.println(String.valueOf(dto));
				}
				else {
					for (Writer out : into) {
						out.write(String.valueOf(dto));
					}
				}
			}
		};
	}


	public static Says threadDto(final Writer... into) {
		return new Says() {
			public void invoke(final Object dto)
				throws IOException {

				final StringBuilder buffer = new StringBuilder();
				buffer.append(Thread.currentThread().getName());

				if (dto != null) {
					buffer.append("\t").append(String.valueOf(dto));
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
			public void invoke(final Object dto)
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
			public void invoke(final Object dto)
				throws IOException {

				if (into.length == 0) {
					final StringBuilder buffer = new StringBuilder();
					buffer.append("[ ").append(Thread.currentThread().getName()).append(" ]").append("\t");
					buffer.append(toSay).append(" ");
					if (dto != null) {
						buffer.append(String.valueOf(dto));
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


	public static Says boom() {
		return new Says() {
			public void invoke(final Object dto)
				throws Exception {

				throw new Exception("boom");
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


		public Says threadDto() {
			return Says.thread(writer);
		}


		public Says thread() {
			return Says.thread(writer);
		}


		public Says nothing() {
			return Says.nothing();
		}


		public Says dto() {
			return Says.dto(writer);
		}


		public Says boom() {
			return Says.boom();
		}


		@Override
		public String toString() {
			return writer.toString();
		}
	}
}
