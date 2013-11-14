package cmd4j;

import java.util.UUID;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import cmd4j.IChain.IUndoChain;
import cmd4j.ICommand.ICommand1;
import cmd4j.ICommand.ICommand3;
import cmd4j.ICommand.IObservableCommand;

/**
 * test observing while undoing and returning
 *
 * @author wassj
 *
 */
public class ObservableUndo3Test {
	final String expected = UUID.randomUUID().toString().substring(0, 7);

	@Mocked
	private ICommand1 before, after, success, failed, results;
	@Mocked
	private ICommand3.IUndo<String> command3;


	@BeforeMethod
	public void setup()
		throws Exception {

		new NonStrictExpectations() {
			{
				command3.invoke();
				result = expected;
				command3.undo();
				result = expected.toUpperCase();
			}
		};
	}


	@AfterMethod
	public void teardown()
		throws Exception {

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
			}
		};
	}


	@Test
	public void successNoReturns()
		throws Exception {

		final IObservableCommand<String> observable = Observers.observable(command3).before(before).after(after).onFailure(failed).onSuccess(success).results(results);
		final IChain<String> chain = Chains.create(observable);
		final IUndoChain<String> undo = Chains.undoable(chain);

		Assert.assertEquals(undo.invoke(), expected);
		Assert.assertEquals(undo.undo(), expected.toUpperCase());
	}


	@Test
	public void successReturns()
		throws Exception {

		final IObservableCommand<String> observable = Observers.observable(command3).before(before).after(after).onFailure(failed).onSuccess(success).results(results);
		final IChain<String> chain = Chains.create(observable);
		final IUndoChain<String> undo = Chains.undoable(chain);

		Assert.assertEquals(undo.invoke(), expected);
		Assert.assertEquals(undo.undo(), expected.toUpperCase());
	}


	@Test
	public void successNoReturnsNested1x()
		throws Exception {

		final IObservableCommand<String> observable = Observers.observable(command3).before(before).after(after).onFailure(failed).onSuccess(success).results(results);
		final IChain<String> chain = Chains.create(observable);
		final IChain<String> wrapped = Observers.observable(chain).before(before).after(after).onFailure(failed).onSuccess(success).results(results);
		final IUndoChain<String> undo = Chains.undoable(wrapped);

		Assert.assertEquals(undo.invoke(), expected);
		Assert.assertEquals(undo.undo(), expected.toUpperCase());
	}


	@Test
	public void successNoReturnsNested2x()
		throws Exception {

		final IObservableCommand<String> observable = Observers.observable(command3).before(before).after(after).onFailure(failed).onSuccess(success).results(results);
		final IChain<String> chain = Chains.create(observable);
		final IChain<String> wrapped = Observers.observable(chain).before(before).after(after).onFailure(failed).onSuccess(success).results(results);
		final IUndoChain<String> undo = Chains.undoable(wrapped);
		final IChain<String> wrapped2 = Observers.observable(undo).before(before).after(after).onFailure(failed).onSuccess(success).results(results);
		final IUndoChain<String> undo2 = Chains.undoable(wrapped2);

		Assert.assertEquals(undo2.invoke(), expected);
		Assert.assertEquals(undo2.undo(), expected.toUpperCase());
	}
}
