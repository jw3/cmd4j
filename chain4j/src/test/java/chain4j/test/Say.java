package chain4j.test;

import chain4j.IChainable;

/**
 * 
 * 
 * @author wassj
 *
 */
abstract public class Say
	implements IChainable {

	public static Say now(final String what) {
		return new Say() {
			public void invoke()
				throws InterruptedException {

				if ("hello".equals(what)) {
					System.out.println(Thread.currentThread().getName());
					//Thread.sleep(5000);
				}
				System.out.println(what + "\t" + Thread.currentThread().getName());
			}
		};
	}


	public static Say boom() {
		return new Say() {
			public void invoke()
				throws Exception {

				throw new Exception("boom");
			}
		};
	}
}
