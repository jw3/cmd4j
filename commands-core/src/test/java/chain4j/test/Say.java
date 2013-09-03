package chain4j.test;

import chain4j.ICommand2;

/**
 * 
 * 
 * @author wassj
 *
 */
abstract public class Say
	implements ICommand2 {

	public static Say what(final Object o) {
		return new Say() {
			public void invoke(Object dto) {
				final StringBuilder buffer = new StringBuilder();
				buffer.append("[ ").append(Thread.currentThread().getName()).append(" ]").append("\t");
				buffer.append(o).append(" ");

				if (dto != null) {
					buffer.append(String.valueOf(dto));
				}
				System.out.println(buffer.toString());
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


			public void invoke(Object dto)
				throws Exception {
				throw new Exception("boom");
			}
		};
	}
}
