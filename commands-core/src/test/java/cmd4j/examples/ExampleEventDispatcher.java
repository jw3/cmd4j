package cmd4j.examples;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import cmd4j.Chains;
import cmd4j.Commands;
import cmd4j.ICommand;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;
import cmd4j.examples.events.Dispatcher;
import cmd4j.examples.events.IListener;

import com.google.common.util.concurrent.MoreExecutors;

/**
 * Samples of event dispatcher that is implemeted using {@link ICommand commands} 
 *
 * @author wassj
 *
 */
public class ExampleEventDispatcher {

	public static void main(String[] args)
		throws Exception {

		//--------------------------------------------------------------
		Dispatcher.addListener(new IListener<String>() {
			public ICommand handle(String t) {
				return print("string event");
			};
		});

		Dispatcher.addListener(new IListener<Integer>() {
			public ICommand handle(Integer t) {
				return print("integer event");
			};
		});

		Dispatcher.fire("hello world");
		Dispatcher.fire(1);
		//--------------------------------------------------------------

		Dispatcher.clearListeners();

		//--------------------------------------------------------------
		Dispatcher.addListener(new IListener<Integer>() {
			public ICommand handle(Integer t) {
				return countDown(t);
			};
		});
		Dispatcher.fire(11);
		//--------------------------------------------------------------

		Dispatcher.clearListeners();

		//--------------------------------------------------------------
		Dispatcher.addListener(new IListener<String>() {
			public ICommand handle(String t) {
				// specify the edt executor (Event Dispatch Thread) for this listener
				//return Chains.makeThreaded(Chains.create(isEdt()), Concurrent.swingExecutor());
				return Chains.builder().add(isEdt()).executor(Commands.swingExecutor()).build();
			};
		});
		Dispatcher.fire("edt?");
		//--------------------------------------------------------------

		Dispatcher.clearListeners();

		//--------------------------------------------------------------
		Dispatcher.addListener(new IListener<String>() {
			public ICommand handle(String t) {
				final ExecutorService exec = Executors.newSingleThreadExecutor();
				return Chains.builder()//
					.add(sayThread())
					.executor(MoreExecutors.sameThreadExecutor())

					.add(sayThread())
					.executor(Commands.swingExecutor())

					.add(isEdt())
					.input("edt?")
					.executor(exec)

					.add(sayThread())

					.add(isEdt())
					.input("edt?")

					.add(shutdown(exec))

					.build();
			};
		});
		Dispatcher.fire("executing");
	}


	/*
	 * command factories
	 */
	static <T> ICommand print(final String message) {
		return new ICommand2<T>() {
			public void invoke(T input) {
				System.out.println(message + " : " + String.valueOf(input));
			}
		};
	}


	/**
	 * demonstration that {@link ICommand3 command3s} work through the dispatcher
	 */
	static ICommand countDown(final Integer count) {
		return new ICommand4<Integer, ICommand>() {
			public ICommand invoke(Integer original) {
				System.out.println(count + " (" + original + ")");

				final int next = count - 1;
				if (next > 0) {
					return countDown(next);
				}
				return null;
			}
		};
	}


	/**
	 * test to see if this command is executed on the Event Dispatch Thread
	 * @return
	 */
	static ICommand isEdt() {
		return new ICommand2<String>() {
			public void invoke(String message) {
				System.out.println(message + " " + (SwingUtilities.isEventDispatchThread() ? "yes" : "no"));
			}
		};
	}


	/**
	 * say what thread the command is execting on
	 * @return
	 */
	static ICommand sayThread() {
		return new ICommand2<String>() {
			public void invoke(String message) {
				System.out.println(message + " on " + Thread.currentThread().getName());
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
