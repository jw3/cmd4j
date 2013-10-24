package cmd4j;

import java.util.concurrent.ExecutorService;

import cmd4j.Internals.Link.EmptyLink;
import cmd4j.Internals.Link.LinkBuilder;
import cmd4j.Internals.Link.LinkUndoDecorator;

/**
 * Utility methods for {@link ILink links}
 * 
 * @author wassj
 *
 */
public enum Links {
	/*singleton-enum*/;

	/**
	 * creates an empty {@link ILink link} that can be used anywhere a normal link is used but will not do anything 
	 */
	public static ILink empty() {
		return new EmptyLink();
	}


	public static ILink create(final ICommand command) {
		return new LinkBuilder(command).build();
	}


	public static ILink create(final ICommand command, ExecutorService executor) {
		return new LinkBuilder(command).executor(executor).build();
	}


	static ILink undo(final ICommand command) {
		return new LinkUndoDecorator(Links.create(command));
	}


	static ILink undo(final ILink link) {
		return new LinkUndoDecorator(link);
	}
}
