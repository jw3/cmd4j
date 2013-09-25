package cmd4j;

import java.io.IOException;
import java.io.Writer;

/**
 * 
 * 
 * @author wassj
 *
 */
abstract public class Say
	implements ICommand2 {

	public static Say nothing() {
		return new Say() {
			public void invoke(final Object dto) {
			}
		};
	}


	public static Say dto(final Writer... into) {
		return new Say() {
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


	public static Say thread(final Writer... into) {
		return new Say() {
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


	public static Say what(final Object toSay, final Object... into) {
		return new Say() {
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


	public static Say boom() {
		return new Say() {
			public void invoke(final Object dto)
				throws Exception {

				throw new Exception("boom");
			}
		};
	}
}
