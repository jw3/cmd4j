package chain4j.builder;

import java.util.Iterator;

import chain4j.ICommand;
import chain4j.ICommand2;

import com.google.common.collect.Iterators;

/**
 * {@link ICommand} utilities
 *
 * @author wassj
 *
 */
public enum Commands {
	/*singleton-enum*/;

	public static ICommand nop() {
		return new ICommand() {
			public void invoke() {
			}
		};
	}


	public static ICommand nop2() {
		return new ICommand2() {
			public void invoke() {
			}


			public void invoke(Object dto) {
			}
		};
	}


	public static Iterator<ICommand> nopIterator() {
		return Iterators.singletonIterator(nop());
	}


	public static Iterator<ICommand> nopIterator2() {
		return Iterators.singletonIterator(nop2());
	}
}
