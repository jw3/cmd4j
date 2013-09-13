package chain4j.decorator;

import java.util.Iterator;

import chain4j.ICommand;
import chain4j.ILink;

import com.google.common.util.concurrent.FutureCallback;

/**
 * A decorator that adds success and failure handler to an ILink
 *
 * @author wassj
 *
 */
abstract public class LinkCallbackDecorator
	implements ILink, FutureCallback<ILink> {

	private final ILink link;


	public LinkCallbackDecorator(final ILink link) {
		this.link = link;
	}


	/**
	 * Override to provide code that executes on successful completion of the decorated link
	 */
	public void onSuccess(ILink result) {
	}


	/**
	 * Override to provide code that executes on failure of the decorated link
	 */
	public void onFailure(Throwable t) {
	}


	final public ILink call()
		throws Exception {

		return link.call();
	}


	final public Iterator<ICommand> iterator() {
		return link.iterator();
	}


	final public ILink next() {
		return link.next();
	}


	final public Object dto() {
		return link.dto();
	}


	final public ILink dto(Object dto) {
		link.dto(dto);
		return this;
	}
}
