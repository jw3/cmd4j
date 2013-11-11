package cmd4j;

import mockit.Mocked;
import mockit.Verifications;

import org.testng.annotations.Test;

import cmd4j.IChain.IUndoChain;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand2;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.ICommand4;
import cmd4j.ICommand.IObservableCommand;

/**
 * test observing while undoing
 * 
 * this will implicitly test "stacking" of decorators
 *
 * @author wassj
 *
 */
public class ObservableUndoTest {

	@Mocked
	private ICommand1 before, after, success, failed, results;
	@Mocked
	private ICommand1.IUndo command1;
	@Mocked
	private ICommand2.IUndo<Object> command2;
	@Mocked
	private ICommand3.IUndo<Object> command3;
	@Mocked
	private ICommand4.IUndo<Object, Object> command4;


	@Test
	public void success()
		throws Exception {

		final IObservableCommand<Void> observable = Observers.observable(command1).before(before).after(after).onFailure(failed).onSuccess(success).results(results);
		final IChain<Void> chain = Chains.create(observable);
		final IUndoChain<Void> undo = Chains.undoable(chain);
		undo.invoke();
		undo.undo();

		new Verifications() {
			{
				before.invoke();
				times = 2;
				after.invoke();
				times = 2;
				success.invoke();
				times = 2;
				results.invoke();
				times = 2;
				failed.invoke();
				times = 0;

				command1.invoke();
				times = 1;
				command1.undo();
				times = 1;
			}
		};
	}
}
