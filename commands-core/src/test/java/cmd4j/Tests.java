package cmd4j;

/**
 * Cmd4j Test Utils
 *
 * @author wassj
 *
 */
public enum Tests {
	/*singleton-enum*/;

	public ITestableCommand nop1() {
		return new TestableCommand1();
	}


	public ITestableCommand nop2() {
		return new TestableCommand2();
	}


	public ITestableCommand nop3() {
		return new TestableCommand3();
	}


	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public abstract static class BaseTestableCommand
		implements ITestableCommand {

		protected boolean invoked;


		public boolean invoked() {
			return invoked;
		}
	}


	private static class TestableCommand1
		extends BaseTestableCommand
		implements ICommand1 {

		public void invoke() {
			invoked = true;
		}
	}


	private static class TestableCommand2
		extends BaseTestableCommand
		implements ICommand2 {

		public void invoke(final Object dto) {
			invoked = true;
		}
	}


	private static class TestableCommand3
		extends BaseTestableCommand
		implements ICommand3 {

		public ICommand invoke(final Object dto) {
			invoked = true;
			return null;
		}
	}
}
