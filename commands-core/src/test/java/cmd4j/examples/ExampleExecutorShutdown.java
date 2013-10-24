package cmd4j.examples;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cmd4j.Chains;
import cmd4j.Executors2;
import cmd4j.ICommand;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;

/**
 *
 *
 * @author wassj
 *
 */
public class ExampleExecutorShutdown {

	public static void main(String[] args)
		throws Exception {

		final ExecutorService exec = Executors.newSingleThreadExecutor();
		Chains.builder()//
			.add(sayThread())
			.executor(Executors2.sameThreadExecutor())

			.add(sayThread())
			.executor(exec)

			.add(sayThread())

			.add(shutdown(exec))

			.build()
			.invoke();
	}


	static ICommand sayThread() {
		return new ICommand2<String>() {
			public void invoke(String message) {
				System.out.println("on " + Thread.currentThread().getName());
			}
		};
	}


	static ICommand shutdown(final ExecutorService executor) {
		return new ICommand1() {
			public void invoke() {
				executor.shutdown();
			}
		};
	}
}
