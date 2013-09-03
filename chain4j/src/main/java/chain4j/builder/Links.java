package chain4j.builder;

import java.util.concurrent.ExecutorService;

import chain4j.IChainable;
import chain4j.internal.Link;

/**
 * 
 * @author wassj
 *
 */
public class Links {

	public static Link create(final IChainable chainable) {
		return new LinkBuilder(chainable).build();
	}


	public static Link create(final IChainable chainable, ExecutorService executor) {
		return new LinkBuilder(chainable).executor(executor).build();
	}
}
