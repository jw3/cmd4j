package chain4j.decorator;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import chain4j.ICommand;
import chain4j.ILink;
import chain4j.IThreaded;

import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Decorate an {@link ILink} with an {@link ExecutorService} to provide threading capability
 *
 * @author wassj
 *
 */
public class LinkThreadingDecorator
	implements ILink, IThreaded {

	private final ILink link;
	private final ListeningExecutorService executor;


	public LinkThreadingDecorator(final ILink link, final ListeningExecutorService executor) {
		this.link = link;
		this.executor = executor;
	}


	public ListeningExecutorService executor() {
		return executor;
	}


	public ILink next() {
		return link.next();
	}


	public Object dto() {
		return link.dto();
	}


	public ILink dto(Object dto) {
		return link.dto(dto);
	}


	public ILink call()
		throws Exception {

		return link.call();
	}


	public Iterator<ICommand> iterator() {
		return link.iterator();
	}
}
