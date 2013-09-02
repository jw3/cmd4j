package chain4j.internal;

import java.util.concurrent.ExecutorService;

import chain4j.IChain;

/**
 * Empty NOP chain
 * 
 * @author wassj
 *
 */
public class EmptyChain
	implements IChain {

	public void exec() {
	}


	public boolean isEmpty() {
		return true;
	}


	public Link head() {
		return null;
	}


	public IChain dto(Object dto) {
		return this;
	}


	public IChain executor(ExecutorService executor) {
		return this;
	}
}
