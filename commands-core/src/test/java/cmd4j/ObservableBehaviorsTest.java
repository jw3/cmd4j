package cmd4j;

import mockit.Mocked;
import mockit.Verifications;

import org.testng.annotations.Test;

import cmd4j.ICommand.ICommand1;
import cmd4j.testing.Does;

/**
 * validate the execution of observer commands
 *
 * @author wassj
 *
 */
public class ObservableBehaviorsTest {

	@Mocked
	private ICommand1 before, after, success, failed, results;


	@Test
	public void commandOnSuccess()
		throws Exception {

		final ICommand command = Observers.observable(Does.nothing()).before(before).after(after).onFailure(failed).onSuccess(success).results(results);
		Chains.create(command).invoke();

		new Verifications() {
			{
				before.invoke();
				times = 1;
				after.invoke();
				times = 1;
				success.invoke();
				times = 1;
				results.invoke();
				times = 1;
				failed.invoke();
				times = 0;
			}
		};
	}


	@Test(expectedExceptions = Exception.class)
	public void commandOnFailure()
		throws Exception {

		final ICommand command = Observers.observable(Does.boom()).before(before).after(after).onFailure(failed).onSuccess(success).results(results);

		try {
			Chains.create(command).invoke();
		}
		finally {
			new Verifications() {
				{
					before.invoke();
					times = 1;
					after.invoke();
					times = 1;
					success.invoke();
					times = 0;
					results.invoke();
					times = 0;
					failed.invoke();
					times = 1;
				}
			};
		}
	}


	@Test
	public void chainOnSuccess()
		throws Exception {

		final IChain<Void> chain = Observers.observable(Chains.create(Does.nothing())).before(before).after(after).onFailure(failed).onSuccess(success).results(results);
		chain.invoke();

		new Verifications() {
			{
				before.invoke();
				times = 1;
				after.invoke();
				times = 1;
				success.invoke();
				times = 1;
				results.invoke();
				times = 1;
				failed.invoke();
				times = 0;
			}
		};
	}


	@Test(expectedExceptions = Exception.class)
	public void chainOnFailure()
		throws Exception {

		final IChain<Void> chain = Observers.observable(Chains.create(Does.boom())).before(before).after(after).onFailure(failed).onSuccess(success).results(results);

		try {
			chain.invoke();
		}
		finally {
			new Verifications() {
				{
					before.invoke();
					times = 1;
					after.invoke();
					times = 1;
					success.invoke();
					times = 0;
					results.invoke();
					times = 0;
					failed.invoke();
					times = 1;
				}
			};
		}
	}
}
