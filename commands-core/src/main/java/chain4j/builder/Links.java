package chain4j.builder;

import java.util.concurrent.ExecutorService;

import chain4j.ICommand;
import chain4j.ILink;
import chain4j.internal.Link;
import chain4j.internal.Linker;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * Utility methods for {@link Link}s
 * 
 * @author wassj
 *
 */
public enum Links {
	/*singleton-enum*/;

	public static void execute(final ILink link)
		throws Exception {

		final Linker linker = Linker.unthreaded(link, null);
		MoreExecutors.sameThreadExecutor().submit(linker).get();
	}


	public static ILink create(final ICommand command) {
		return new LinkBuilder(command).build();
	}


	public static ILink create(final ICommand command, ExecutorService executor) {
		return new LinkBuilder(command).executor(executor).build();
	}
}
