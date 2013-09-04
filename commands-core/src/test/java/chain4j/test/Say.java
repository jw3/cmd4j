package chain4j.test;

import java.io.OutputStream;
import java.io.PrintStream;

import chain4j.ICommand2;

/**
 * 
 * 
 * @author wassj
 *
 */
abstract public class Say
	implements ICommand2 {

	private OutputStream output = System.out;


	public Say into(OutputStream output) {
		this.output = output;
		return this;
	}


	public OutputStream into() {
		return output;
	}


	public static Say nothing() {
		return new Say() {
			public void invoke(final Object dto) {
			}


			public void invoke() {
			}
		};
	}


	public static Say what(final Object toSay) {
		return new Say() {
			public void invoke(final Object dto) {
				final StringBuilder buffer = new StringBuilder();
				buffer.append("[ ").append(Thread.currentThread().getName()).append(" ]").append("\t");
				buffer.append(toSay).append(" ");

				if (dto != null) {
					buffer.append(String.valueOf(dto));
				}
				new PrintStream(into()).println(buffer.toString());
			}


			public void invoke() {
				this.invoke(null);
			}
		};
	}


	public static Say boom() {
		return new Say() {
			public void invoke()
				throws Exception {
				this.invoke(null);
			}


			public void invoke(final Object dto)
				throws Exception {

				throw new Exception("boom");
			}
		};
	}
}
