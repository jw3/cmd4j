package chain4j.internal;

import java.util.Iterator;

import chain4j.ICommand;
import chain4j.ILink;
import chain4j.common.ICommandUndo;

/**
 *
 *
 * @author wassj
 *
 */
public class LinkUndoDecorator
	implements ILink {

	private final ILink link;


	public static ILink decorate(ILink link) {
		return new LinkUndoDecorator(link);
	}


	private LinkUndoDecorator(final ILink link) {
		this.link = link;
	}


	public ILink call()
		throws Exception {

		final ICommand command = link.iterator().next();
		if (command instanceof ICommandUndo) {
			((ICommandUndo)command).undo();
		}
		else {
			command.invoke();
		}
		return next();
	}


	public Iterator<ICommand> iterator() {
		return link.iterator();
	}


	public ILink next() {
		return link.next();
	}


	public Object dto() {
		return link.dto();
	}


	public ILink dto(Object dto) {
		link.dto(dto);
		return this;
	}
}
