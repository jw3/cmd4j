package chain4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * {@link ThreadFactory} for an {@link ExecutorService} that provides an {@link AssertThread} on the Thread it created 
 *
 * @author wassj
 *
 */
public class AssertableThreadFactory
	implements ThreadFactory {

	private Thread thread;


	public static AssertableThreadFactory create() {
		return new AssertableThreadFactory();
	}


	public AssertThread assertThis() {
		return new AssertThread() {
			public Thread getExpected() {
				return thread;
			}
		};
	}


	public Thread newThread(Runnable r) {
		return thread = new Thread(r);
	}


	private AssertableThreadFactory() {
	}
}
