package chain4j.builder;

import java.util.concurrent.ExecutorService;

import chain4j.ICommand;
import chain4j.ILink;
import chain4j.internal.Link;

/**
 * Utility methods for {@link Link}s
 * 
 * @author wassj
 *
 */
public class Links {

	public static ILink create(final ICommand command) {
		return new LinkBuilder(command).build();
	}


	public static ILink create(final ICommand command, ExecutorService executor) {
		return new LinkBuilder(command).executor(executor).build();
	}
}
